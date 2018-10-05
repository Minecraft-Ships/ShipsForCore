package org.ships.algorthum.blockfinder;

import org.core.world.position.BlockPosition;
import org.ships.algorthum.Algorthum;
import org.ships.vessel.structure.PositionableShipsStructure;

public interface BasicBlockFinder extends Algorthum {

    PositionableShipsStructure getConnectedBlocks(BlockPosition position);
    void getConnectedBlocksOvertime(BlockPosition position, OvertimeBlockFinderUpdate runAfterFullSearch);
    int getBlockLimit();
    BasicBlockFinder setBlockLimit(int limit);

}
