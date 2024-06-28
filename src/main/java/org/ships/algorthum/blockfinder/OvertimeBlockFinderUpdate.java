package org.ships.algorthum.blockfinder;

import org.core.vector.type.Vector3;
import org.jetbrains.annotations.NotNull;
import org.ships.vessel.structure.PositionableShipsStructure;

public interface OvertimeBlockFinderUpdate {

    enum BlockFindControl {

        IGNORE,
        USE,
        USE_AND_FINISH

    }

    BlockFindControl onBlockFind(@NotNull PositionableShipsStructure currentStructure,
                                 @NotNull Vector3<Integer> blockPosition);

}
