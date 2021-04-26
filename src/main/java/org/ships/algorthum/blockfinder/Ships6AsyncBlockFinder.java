package org.ships.algorthum.blockfinder;

import org.core.CorePlugin;
import org.core.schedule.unit.TimeUnit;
import org.core.vector.type.Vector3;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.async.ASyncBlockPosition;
import org.ships.algorthum.blockfinder.exact.ExactBlockFinder;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;
import org.ships.config.configuration.ShipsConfig;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.AbstractPosititionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;
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
    public Ships6AsyncBlockFinder init() {
        ShipsPlugin plugin = ShipsPlugin.getPlugin();
        ShipsConfig config = plugin.getConfig();
        this.limit = config.getDefaultTrackSize();
        this.list = ShipsPlugin.getPlugin().getBlockList();
        return this;
    }

    @Override
    public void getConnectedBlocksOvertime(BlockPosition position, OvertimeBlockFinderUpdate runAfterFullSearch) {
        CorePlugin
                .createSchedulerBuilder()
                .setAsync(true)
                .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                .setDelay(0)
                .setExecutor(() -> {
            PositionableShipsStructure structure = new AbstractPosititionableShipsStructure(Position.toSync(position));
            Set<ASyncBlockPosition> toProcess = new HashSet<>();
            Direction[] directions = Direction.withYDirections(FourFacingDirection.getFourFacingDirections());
            toProcess.add(Position.toASync(position));
            while(!toProcess.isEmpty() && structure.getOriginalRelativePositions().size() < this.limit){
                Set<Vector3<Integer>> positions = structure.getOriginalRelativePositions();
                Set<ASyncBlockPosition> next = Collections.synchronizedSet(new HashSet<>());
                for(ASyncBlockPosition pos : toProcess){
                    final Set<ASyncBlockPosition> finalToProcess = toProcess;
                    Stream.of(directions).forEach(direction -> {
                        ASyncBlockPosition block = pos.getRelative(direction);
                        Vector3<Integer> vector = block.getPosition().minus(position.getPosition());
                        BlockInstruction bi = this.list.getBlockInstruction(block.getBlockType());
                        if(bi.getCollideType().equals(BlockInstruction.CollideType.MATERIAL)){
                            if(positions.contains(vector) || finalToProcess.contains(block)){
                                return;
                            }
                            if(next.parallelStream().noneMatch(b -> b.getPosition().equals(block.getPosition()))) {
                                next.add(block);
                            }
                        }
                    });
                    if(!runAfterFullSearch.onBlockFind(structure, pos)){
                        continue;
                    }
                    structure.addPosition(Position.toSync(pos));
                }
                toProcess = next;
            }
            CorePlugin
                    .createSchedulerBuilder()
                    .setDelay(0)
                    .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                    .setExecutor(() -> runAfterFullSearch.onShipsStructureUpdated(structure))
                    .setDisplayName("Ships 6 async release")
                    .build(ShipsPlugin.getPlugin())
                    .run();
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
    public BasicBlockFinder setBlockLimit(int limit) {
this.limit = limit;
        return this;
    }

    @Override
    public Optional<Vessel> getConnectedVessel() {
        return Optional.ofNullable(this.vessel);
    }

    @Override
    public BasicBlockFinder setConnectedVessel(Vessel vessel) {
        this.vessel = vessel;
        return this;
    }

    @Override
    public ExactBlockFinder getTypeFinder() {
        throw new IllegalStateException("Not implemented");
    }
}
