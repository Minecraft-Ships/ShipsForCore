package org.ships.algorthum.blockfinder;

import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.vessel.structure.PositionableShipsStructure;

public interface OvertimeBlockFinderUpdate {

    enum BlockFindControl {

        IGNORE,
        USE,
        USE_AND_FINISH

    }

    BlockFindControl onBlockFind(@NotNull PositionableShipsStructure currentStructure, @NotNull BlockPosition block);

}
