package org.ships.algorthum.blockfinder;

import org.core.TranslateCore;
import org.core.schedule.unit.TimeUnit;
import org.core.vector.type.Vector3;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.async.ASyncBlockPosition;
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

public class Ships6MultiAsyncBlockFinder implements BasicBlockFinder {

    protected int limit;
    private Vessel vessel;
    private BlockList list;

    @Override
    public String getId() {
        return "ships:blockfinder_ships_six_async_multi";
    }

    @Override
    public String getName() {
        return "Ships 6 R1 ASync BlockFinder";
    }

    @Override
    public @NotNull Ships6MultiAsyncBlockFinder init() {
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
                    LinkedTransferQueue<Map.Entry<ASyncBlockPosition, Direction>> toProcess = new LinkedTransferQueue<>();
                    Direction[] directions = Direction.withYDirections(FourFacingDirection.getFourFacingDirections());
                    toProcess.add(new AbstractMap.SimpleImmutableEntry<>(position.toAsyncPosition(),
                                                                         FourFacingDirection.NONE));
                    while (!toProcess.isEmpty() && structure.size() < limit && !ShipsPlugin
                            .getPlugin()
                            .isShuttingDown()) {
                        Collection<Vector3<Integer>> positions = structure.getVectorsRelativeToLicence().collect(
                                Collectors.toList());
                        LinkedTransferQueue<Map.Entry<ASyncBlockPosition, Direction>> next = new LinkedTransferQueue<>();
                        while (toProcess.hasWaitingConsumer()) {
                            continue;
                        }
                        final Collection<Map.Entry<ASyncBlockPosition, Direction>> finalToProcess = toProcess;
                        AtomicBoolean shouldKill = new AtomicBoolean();
                        toProcess.parallelStream().forEach(posEntry -> {
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
                                        ASyncBlockPosition block = posEntry.getKey().getRelative(direction);
                                        Vector3<Integer> vector = block.getPosition().minus(position.getPosition());
                                        BlockInstruction bi = this.list.getBlockInstruction(block.getBlockType());
                                        if (bi.getCollide() == CollideType.MATERIAL) {
                                            if (positions.contains(vector) || finalToProcess
                                                    .parallelStream()
                                                    .anyMatch(entry -> entry.getKey().equals(block))) {
                                                return;
                                            }
                                            if (next
                                                    .parallelStream()
                                                    .noneMatch(b -> b
                                                            .getKey()
                                                            .getPosition()
                                                            .equals(block.getPosition()))) {
                                                next.add(new AbstractMap.SimpleImmutableEntry<>(block, direction));
                                            }
                                        }
                                    });
                            OvertimeBlockFinderUpdate.BlockFindControl blockFind = runAfterFullSearch.onBlockFind(
                                    structure, posEntry.getKey().getPosition());
                            if (blockFind == OvertimeBlockFinderUpdate.BlockFindControl.IGNORE) {
                                return;
                            }
                            structure.addPositionRelativeToWorld(posEntry.getKey().toSyncPosition());
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
