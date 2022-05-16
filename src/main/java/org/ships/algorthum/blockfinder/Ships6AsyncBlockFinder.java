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
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;
import org.ships.config.configuration.ShipsConfig;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.AbstractPositionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

public class Ships6AsyncBlockFinder implements BasicBlockFinder {

    private Vessel vessel;
    protected int limit;
    private BlockList list;

    @Override
    public String getId() {
        return "ships:blockfinder_ships_six_async";
    }

    @Override
    public String getName() {
        return "Ships 6 R1 ASync BlockFinder";
    }

    @Override
    public @NotNull Ships6AsyncBlockFinder init() {
        ShipsPlugin plugin = ShipsPlugin.getPlugin();
        ShipsConfig config = plugin.getConfig();
        this.limit = config.getDefaultTrackSize();
        this.list = ShipsPlugin.getPlugin().getBlockList();
        return this;
    }

    @Override
    public void getConnectedBlocksOvertime(@NotNull BlockPosition position, @NotNull OvertimeBlockFinderUpdate runAfterFullSearch) {
        TranslateCore
                .createSchedulerBuilder()
                .setAsync(true)
                .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                .setDelay(0)
                .setRunner((scheduler) -> {
                    PositionableShipsStructure structure = new AbstractPositionableShipsStructure(Position.toSync(position));
                    Collection<Map.Entry<ASyncBlockPosition, Direction>> toProcess = new HashSet<>();
                    Direction[] directions = Direction.withYDirections(FourFacingDirection.getFourFacingDirections());
                    toProcess.add(new AbstractMap.SimpleImmutableEntry<>(Position.toASync(position),
                            FourFacingDirection.NONE));
                    while (!toProcess.isEmpty() && structure.getOriginalRelativePositions().size() < this.limit) {
                        Set<Vector3<Integer>> positions = structure.getOriginalRelativePositions();
                        Collection<Map.Entry<ASyncBlockPosition, Direction>> next = new LinkedBlockingQueue<>();
                        for (Map.Entry<ASyncBlockPosition, Direction> posEntry : toProcess) {
                            final Collection<Map.Entry<ASyncBlockPosition, Direction>> finalToProcess = toProcess;
                            Stream.of(directions).filter(direction -> !posEntry.getValue().equals(direction.getOpposite())).forEach(direction -> {
                                ASyncBlockPosition block = posEntry.getKey().getRelative(direction);
                                Vector3<Integer> vector = block.getPosition().minus(position.getPosition());
                                BlockInstruction bi = this.list.getBlockInstruction(block.getBlockType());
                                if (bi.getCollideType()==BlockInstruction.CollideType.MATERIAL) {
                                    if (positions.contains(vector) || finalToProcess.parallelStream().anyMatch(entry -> entry.getKey().equals(block))) {
                                        return;
                                    }
                                    if (next.parallelStream().noneMatch(b -> b.getKey().getPosition().equals(block.getPosition()))) {
                                        next.add(new AbstractMap.SimpleImmutableEntry<>(block, direction));
                                    }
                                }
                            });
                            OvertimeBlockFinderUpdate.BlockFindControl blockFind =
                                    runAfterFullSearch.onBlockFind(structure, posEntry.getKey());
                            if (blockFind==OvertimeBlockFinderUpdate.BlockFindControl.IGNORE) {
                                continue;
                            }
                            structure.addPosition(Position.toSync(posEntry.getKey()));
                            if (blockFind==OvertimeBlockFinderUpdate.BlockFindControl.USE_AND_FINISH) {
                                TranslateCore
                                        .createSchedulerBuilder()
                                        .setDelay(0)
                                        .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                                        .setRunner((context) -> runAfterFullSearch.onShipsStructureUpdated(structure))
                                        .setDisplayName("Ships 6 async release")
                                        .build(ShipsPlugin.getPlugin())
                                        .run();
                                scheduler.cancel();

                                return;
                            }

                        }
                        toProcess = next;
                    }
                    TranslateCore
                            .createSchedulerBuilder()
                            .setDelay(0)
                            .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                            .setRunner((context) -> runAfterFullSearch.onShipsStructureUpdated(structure))
                            .setDisplayName("Ships 6 async release")
                            .build(ShipsPlugin.getPlugin())
                            .run();
                    scheduler.cancel();

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
