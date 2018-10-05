package org.ships.algorthum.blockfinder;

import org.core.world.position.BlockPosition;
import org.ships.vessel.structure.PositionableShipsStructure;

public interface OvertimeBlockFinderUpdate {

    public void onShipsStructureUpdated(PositionableShipsStructure structure);
    public boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block);

}
