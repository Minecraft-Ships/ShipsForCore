package org.ships.algorthum.blockfinder;

import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.vessel.structure.PositionableShipsStructure;

public interface OvertimeBlockFinderUpdate {

    void onShipsStructureUpdated(PositionableShipsStructure structure);
    boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block);

}
