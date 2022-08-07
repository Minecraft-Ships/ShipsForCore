package org.ships.exceptions.load;

import org.ships.vessel.structure.PositionableShipsStructure;

public class UnableToFindLicenceSign extends LoadVesselException {

    private final PositionableShipsStructure shipsStructure;

    public UnableToFindLicenceSign(PositionableShipsStructure pss, String reason) {
        super(reason);
        this.shipsStructure = pss;
    }

    public PositionableShipsStructure getFoundStructure() {
        return this.shipsStructure;
    }
}
