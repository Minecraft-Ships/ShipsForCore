package org.ships.algorthum.blockfinder;

import org.core.TranslateCore;
import org.core.schedule.Scheduler;
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
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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
    public void getConnectedBlocksOvertime(@NotNull BlockPosition position,
                                           @NotNull OvertimeBlockFinderUpdate runAfterFullSearch) {
        int limit = this.limit;
        TranslateCore
                .getScheduleManager()
                .schedule()
                .setAsync(true)
                .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                .setDelay(0)
                .setRunner((scheduler) -> {
                    PositionableShipsStructure structure = new AbstractPositionableShipsStructure(
                            Position.toSync(position));
                    LinkedTransferQueue<Map.Entry<ASyncBlockPosition, Direction>> toProcess = new LinkedTransferQueue<>();
                    Direction[] directions = Direction.withYDirections(FourFacingDirection.getFourFacingDirections());
                    toProcess.add(new AbstractMap.SimpleImmutableEntry<>(Position.toASync(position),
                                                                         FourFacingDirection.NONE));
                    while (!toProcess.isEmpty() && structure.getOriginalRelativePositionsToCenter().size() < limit
                            && !ShipsPlugin.getPlugin().isShuttingDown()) {
                        Collection<Vector3<Integer>> positions = structure.getOriginalRelativePositionsToCenter();
                        LinkedTransferQueue<Map.Entry<ASyncBlockPosition, Direction>> next = new LinkedTransferQueue<>();
                        while (toProcess.hasWaitingConsumer()) {
                            continue;
                        }
                        final Collection<Map.Entry<ASyncBlockPosition, Direction>> finalToProcess = toProcess;
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
                                        ASyncBlockPosition block = posEntry.getKey().getRelative(direction);
                                        Vector3<Integer> vector = block.getPosition().minus(position.getPosition());
                                        BlockInstruction bi = this.list.getBlockInstruction(block.getBlockType());
                                        if (bi.getCollide() == CollideType.MATERIAL) {
                                            if (positions.contains(vector) || finalToProcess
                                                    .stream()
                                                    .anyMatch(entry -> entry.getKey().equals(block))) {
                                                return;
                                            }
                                            if (next
                                                    .stream()
                                                    .noneMatch(b -> b
                                                            .getKey()
                                                            .getPosition()
                                                            .equals(block.getPosition()))) {
                                                next.add(new AbstractMap.SimpleImmutableEntry<>(block, direction));
                                            }
                                        }
                                    });
                            OvertimeBlockFinderUpdate.BlockFindControl blockFind = runAfterFullSearch.onBlockFind(
                                    structure, posEntry.getKey());
                            if (blockFind == OvertimeBlockFinderUpdate.BlockFindControl.IGNORE) {
                                return;
                            }
                            structure.addPositionRelativeToWorld(Position.toSync(posEntry.getKey()));
                            if (blockFind == OvertimeBlockFinderUpdate.BlockFindControl.USE_AND_FINISH) {
                                shouldKill.set(true);
                                TranslateCore
                                        .getScheduleManager()
                                        .schedule()
                                        .setDelay(0)
                                        .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                                        .setRunner((context) -> runAfterFullSearch.onShipsStructureUpdated(structure))
                                        .setDisplayName("Ships 6 async release")
                                        .build(ShipsPlugin.getPlugin())
                                        .run();
                                if (scheduler instanceof Scheduler.Native nativeSch) {
                                    nativeSch.cancel();
                                }
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
                            .setRunner((context) -> runAfterFullSearch.onShipsStructureUpdated(structure))
                            .setDisplayName("Ships 6 async release")
                            .build(ShipsPlugin.getPlugin())
                            .run();
                    if (scheduler instanceof Scheduler.Native nativeSch) {
                        nativeSch.cancel();
                    }
                })
                .setDisplayName("Ships 6 async structure finder")
                .build(ShipsPlugin.getPlugin())
                .run();

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
