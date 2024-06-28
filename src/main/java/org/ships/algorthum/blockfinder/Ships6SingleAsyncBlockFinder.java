package org.ships.algorthum.blockfinder;

import org.core.TranslateCore;
import org.core.schedule.unit.TimeUnit;
import org.core.utils.Bounds;
import org.core.vector.type.Vector3;
import org.core.world.chunk.AsyncChunk;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.config.blocks.BlockList;
import org.ships.config.blocks.instruction.BlockInstruction;
import org.ships.config.blocks.instruction.CollideType;
import org.ships.config.configuration.ShipsConfig;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.AbstractPositionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ships6SingleAsyncBlockFinder implements BasicBlockFinder {

    protected int limit;
    private Vessel vessel;
    private BlockList list;

    @Override
    public String getId() {
        return "ships:blockfinder_ships_six_single_async";
    }

    @Override
    public String getName() {
        return "Ships 6 R1 Single ASync BlockFinder";
    }

    @Override
    public @NotNull Ships6SingleAsyncBlockFinder init() {
        ShipsPlugin plugin = ShipsPlugin.getPlugin();
        ShipsConfig config = plugin.getConfig();
        this.limit = config.getDefaultTrackSize();
        this.list = ShipsPlugin.getPlugin().getBlockList();
        return this;
    }

    @Override
    public CompletableFuture<PositionableShipsStructure> getConnectedBlocksOvertime(@NotNull BlockPosition position,
                                                                                    @NotNull OvertimeBlockFinderUpdate runAfterFullSearch) {
        CompletableFuture<PositionableShipsStructure> future = new CompletableFuture<>();
        Vector3<Integer> originalChunkPos = position.getChunkPosition();
        Map<AsyncChunk, Bounds<Integer>> asyncChunks = position
                .getWorld()
                .getChunkExtents()
                .filter(chunk -> chunk.getChunkPosition().distanceSquared(originalChunkPos) <= 2)
                .map(chunk -> chunk.createAsync())
                .collect(Collectors.toMap(c -> c, c -> c.getBounds()));
        int limit = this.limit;
        TranslateCore
                .getScheduleManager()
                .schedule()
                .setAsync(true)
                .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                .setDelay(0)
                .setRunner((scheduler) -> {
                    PositionableShipsStructure structure = new AbstractPositionableShipsStructure(
                            position.toSyncPosition());
                    LinkedTransferQueue<Map.Entry<Vector3<Integer>, Direction>> toProcess = new LinkedTransferQueue<>();
                    Direction[] directions = Direction.withYDirections(FourFacingDirection.getFourFacingDirections());
                    toProcess.add(Map.entry(position.getPosition(), FourFacingDirection.NONE));
                    while (!toProcess.isEmpty() && structure.size() < limit && !ShipsPlugin
                            .getPlugin()
                            .isShuttingDown()) {
                        Collection<Vector3<Integer>> positions = structure
                                .getVectorsRelativeToWorld()
                                .collect(Collectors.toList());
                        LinkedTransferQueue<Map.Entry<Vector3<Integer>, Direction>> next = new LinkedTransferQueue<>();
                        while (toProcess.hasWaitingConsumer()) {
                            continue;
                        }
                        final Collection<Map.Entry<Vector3<Integer>, Direction>> finalToProcess = toProcess;
                        AtomicBoolean shouldKill = new AtomicBoolean();
                        toProcess.forEach(posEntry -> {
                            if (ShipsPlugin.getPlugin().isShuttingDown()) {
                                shouldKill.set(true);
                                return;
                            }
                            Stream
                                    .of(directions)
                                    .filter(direction -> !posEntry.getValue().equals(direction.getOpposite()))
                                    .forEach(direction -> {
                                        if (shouldKill.get()) {
                                            return;
                                        }
                                        Vector3<Integer> blockPosition = posEntry
                                                .getKey()
                                                .plus(direction.getAsVector());
                                        BlockDetails blockDetails = asyncChunks
                                                .entrySet()
                                                .stream()
                                                .filter(entry -> entry.getValue().containsWithoutMax(blockPosition))
                                                .map(entry -> entry.getKey().getDetails(blockPosition))
                                                .findFirst()
                                                .orElseThrow(() -> new RuntimeException(
                                                        "Cannot find " + blockPosition + " in loaded chunks"));
                                        BlockInstruction bi = this.list.getBlockInstruction(blockDetails.getType());
                                        if (bi.getCollide() == CollideType.MATERIAL) {
                                            if (positions.contains(blockPosition) || finalToProcess
                                                    .stream()
                                                    .anyMatch(entry -> entry.getKey().equals(blockPosition))) {
                                                return;
                                            }
                                            if (next.stream().noneMatch(b -> b.getKey().equals(blockPosition))) {
                                                next.add(new AbstractMap.SimpleImmutableEntry<>(blockPosition,
                                                                                                direction));
                                            }
                                        }
                                    });
                            OvertimeBlockFinderUpdate.BlockFindControl blockFind = runAfterFullSearch.onBlockFind(
                                    structure, posEntry.getKey());
                            if (blockFind == OvertimeBlockFinderUpdate.BlockFindControl.IGNORE) {
                                return;
                            }

                            Vector3<Integer> vector = posEntry.getKey().minus(structure.getPosition().getPosition());
                            structure.addPositionRelativeToCenter(vector);

                            if (blockFind == OvertimeBlockFinderUpdate.BlockFindControl.USE_AND_FINISH) {
                                shouldKill.set(true);
                                TranslateCore
                                        .getScheduleManager()
                                        .schedule()
                                        .setDelay(0)
                                        .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                                        .setRunner((context) -> future.complete(structure))
                                        .setDisplayName("Ships 6 async release")
                                        .buildDelayed(ShipsPlugin.getPlugin())
                                        .run();
                                scheduler.cancel();
                            }
                        });
                        if (shouldKill.get()) {
                            return;
                        }
                        toProcess = next;
                    }
                    TranslateCore
                            .getScheduleManager()
                            .schedule()
                            .setDelay(0)
                            .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                            .setRunner((context) -> future.complete(structure))
                            .setDisplayName("Ships 6 async release")
                            .buildDelayed(ShipsPlugin.getPlugin())
                            .run();
                    scheduler.cancel();
                })
                .setDisplayName("Ships 6 async structure finder")
                .buildDelayed(ShipsPlugin.getPlugin())
                .run();
        return future;
    }

    @Override
    public int getBlockLimit() {
        return this.limit;
    }

    @Override
    public @NotNull BasicBlockFinder setBlockLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public Optional<Vessel> getConnectedVessel() {
        return Optional.ofNullable(this.vessel);
    }

    @Override
    public @NotNull BasicBlockFinder setConnectedVessel(@Nullable Vessel vessel) {
        this.vessel = vessel;
        return this;
    }
}
