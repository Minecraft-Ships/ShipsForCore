package org.ships.algorthum.blockfinder;

import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.vessel.common.assits.WaterType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

public class FindAirOvertimeBlockFinderUpdate implements OvertimeBlockFinderUpdate {

    private final @NotNull OvertimeBlockFinderUpdate update;
    private final @NotNull Vessel vessel;

    public FindAirOvertimeBlockFinderUpdate(@NotNull Vessel vessel, @NotNull OvertimeBlockFinderUpdate update) {
        this.update = update;
        this.vessel = vessel;
    }

    @Override
    public void onShipsStructureUpdated(@NotNull PositionableShipsStructure structure) {
        if (this.vessel.getType() instanceof WaterType) {
            structure.addAir(this.update::onShipsStructureUpdated);
            return;
        }
        this.update.onShipsStructureUpdated(structure);
    }

    @Override
    public BlockFindControl onBlockFind(@NotNull PositionableShipsStructure currentStructure, @NotNull BlockPosition block) {
        return this.update.onBlockFind(currentStructure, block);
    }
}
