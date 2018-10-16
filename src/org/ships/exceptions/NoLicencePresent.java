package org.ships.exceptions;

import org.ships.vessel.common.types.ShipsVessel;

import java.io.IOException;

public class NoLicencePresent extends IOException {

    public NoLicencePresent(ShipsVessel vessel){
        super("Could not find Ships licence sign at " + vessel.getPosition().getX() + ", " + vessel.getPosition().getY() + ", " + vessel.getPosition().getZ());
    }
}
