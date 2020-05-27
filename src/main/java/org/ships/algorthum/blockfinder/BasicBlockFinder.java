package org.ships.algorthum.blockfinder;

import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.algorthum.Algorthum;
import org.ships.algorthum.blockfinder.typeFinder.BasicTypeBlockFinder;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;

public interface BasicBlockFinder extends Algorthum {

    Ships5BlockFinder SHIPS_FIVE = new Ships5BlockFinder();
    Ships6BlockFinder SHIPS_SIX = new Ships6BlockFinder();
    Ships6AsyncBlockFinder SHIPS_SIX_ASYNC = new Ships6AsyncBlockFinder();

    BasicBlockFinder init();
    PositionableShipsStructure getConnectedBlocks(BlockPosition position);
    void getConnectedBlocksOvertime(BlockPosition position, OvertimeBlockFinderUpdate runAfterFullSearch);
    int getBlockLimit();
    BasicBlockFinder setBlockLimit(int limit);
    Optional<Vessel> getConnectedVessel();
    BasicBlockFinder setConnectedVessel(Vessel vessel);
    BasicTypeBlockFinder getTypeFinder();

}
