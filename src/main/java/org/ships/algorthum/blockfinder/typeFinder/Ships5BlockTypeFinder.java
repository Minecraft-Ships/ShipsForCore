package org.ships.algorthum.blockfinder.typeFinder;

import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;
import org.ships.config.blocks.BlockListable;
import org.ships.config.configuration.ShipsConfig;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;
import java.util.function.Predicate;

public class Ships5BlockTypeFinder implements BasicTypeBlockFinder {

    private int blockLimit;
    private int blockCount = 0;
    private Vessel vessel;
    private BlockList list;
    private Predicate<SyncBlockPosition> predicate;

    private Optional<SyncBlockPosition> getNextBlock(SyncBlockPosition position, Direction... directions){
        if(this.blockLimit != -1 && this.blockCount >= this.blockLimit){
            return Optional.empty();
        }
        this.blockCount++;
        for(Direction direction : directions){
            SyncBlockPosition block = position.getRelative(direction);
            BlockInstruction bi = list.getBlockInstruction(block.getBlockType());
            if(bi.getCollideType().equals(BlockInstruction.CollideType.MATERIAL)){
                if (!predicate.test(block)){
                    Optional<SyncBlockPosition> opBlock = getNextBlock(block, directions);
                    if(opBlock.isPresent()){
                        return opBlock;
                    }
                    continue;
                }
                return Optional.of(block);
            }
        }
        return Optional.empty();
    }

    private Optional<SyncBlockPosition> getBlock(SyncBlockPosition position){
        this.blockCount = 0;
        this.list = ShipsPlugin.getPlugin().getBlockList();
        Direction[] directions = Direction.withYDirections(FourFacingDirection.getFourFacingDirections());
        return getNextBlock(position, directions);
    }

    @Override
    public Ships5BlockTypeFinder init() {
        ShipsPlugin plugin = ShipsPlugin.getPlugin();
        ShipsConfig config = plugin.getConfig();
        this.blockLimit = config.getDefaultTrackSize();
        return this;
    }

    @Override
    public Optional<SyncBlockPosition> findBlock(SyncBlockPosition position, Predicate<SyncBlockPosition> predicate) {
        this.predicate = predicate;
        return getBlock(position);
    }

    @Override
    public void findBlock(SyncBlockPosition position, Predicate<SyncBlockPosition> predicate, OvertimeBlockTypeFinderUpdate runAfterSearch) {
        Optional<SyncBlockPosition> opPosition = findBlock(position, predicate);
        if(opPosition.isPresent()){
            runAfterSearch.onBlockFound(opPosition.get());
        }else{
            runAfterSearch.onFailedToFind();
        }
    }

    @Override
    public int getBlockLimit() {
        return this.blockLimit;
    }

    @Override
    public BasicTypeBlockFinder setBlockLimit(int limit) {
        this.blockLimit = limit;
        return this;
    }

    @Override
    public Optional<Vessel> getConnectedVessel() {
        return Optional.ofNullable(this.vessel);
    }

    @Override
    public BasicTypeBlockFinder setConnectedVessel(Vessel vessel) {
        this.vessel = vessel;
        if(this.vessel != null && this.vessel instanceof BlockListable){
            this.list = ((BlockListable)this.vessel).getBlockList();
        }else{
            this.list = ShipsPlugin.getPlugin().getBlockList();
        }
        return this;
    }

    @Override
    public String getId() {
        return "ships:blockfinder_ships_five";
    }

    @Override
    public String getName() {
        return "Ships 5 BlockFinder";
    }
}
