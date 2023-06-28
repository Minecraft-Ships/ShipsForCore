package org.ships.algorthum.blockfinder;

import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

@Deprecated(forRemoval = true)
public class FindAirOvertimeBlockFinderUpdate implements OvertimeBlockFinderUpdate {

    private final @NotNull OvertimeBlockFinderUpdate update;

    public FindAirOvertimeBlockFinderUpdate(@NotNull Vessel vessel, @NotNull OvertimeBlockFinderUpdate update) {
        this.update = update;
    }

    @Override
    public BlockFindControl onBlockFind(@NotNull PositionableShipsStructure currentStructure,
                                        @NotNull BlockPosition block) {
        return this.update.onBlockFind(currentStructure, block);
    }
}
