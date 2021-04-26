package org.ships.algorthum.blockfinder;

import org.core.world.position.impl.BlockPosition;
import org.ships.algorthum.Algorthum;
import org.ships.algorthum.blockfinder.exact.ExactBlockFinder;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;

public interface BasicBlockFinder extends Algorthum {

    Ships5BlockFinder SHIPS_FIVE = new Ships5BlockFinder();
    Ships5AsyncBlockFinder SHIPS_FIVE_ASYNC = new Ships5AsyncBlockFinder();
    Ships6BlockFinder SHIPS_SIX = new Ships6BlockFinder();
    Ships6AsyncBlockFinder SHIPS_SIX_RELEASE_ONE_ASYNC = new Ships6AsyncBlockFinder();

    BasicBlockFinder init();
    void getConnectedBlocksOvertime(BlockPosition position, OvertimeBlockFinderUpdate runAfterFullSearch);
    int getBlockLimit();
    BasicBlockFinder setBlockLimit(int limit);
    Optional<Vessel> getConnectedVessel();
    BasicBlockFinder setConnectedVessel(Vessel vessel);
    ExactBlockFinder getTypeFinder();

}
