package org.ships.event.vessel;

import org.core.event.events.Cancellable;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

public class VesselStructureUpdate implements VesselEvent, Cancellable {

    private final Vessel vessel;
    private final PositionableShipsStructure newStructure;
    private boolean cancellable;

    public VesselStructureUpdate(PositionableShipsStructure newStructure, Vessel vessel) {
        this.newStructure = newStructure;
        this.vessel = vessel;
    }

    public PositionableShipsStructure getNewStructure() {
        return this.newStructure;
    }

    public PositionableShipsStructure getOldStructure() {
        return this.getVessel().getStructure();
    }

    @Override
    public Vessel getVessel() {
        return this.vessel;
    }

    @Override
    public boolean isCancelled() {
        return this.cancellable;
    }

    @Override
    public void setCancelled(boolean value) {
        this.cancellable = value;
    }
}
