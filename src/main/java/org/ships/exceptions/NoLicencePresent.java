package org.ships.exceptions;

import org.core.world.position.Positionable;
import org.core.world.position.impl.BlockPosition;
import org.ships.vessel.common.assits.FileBasedVessel;

import java.io.IOException;

public class NoLicencePresent extends IOException {

    public NoLicencePresent(Positionable<BlockPosition> vessel) {
        super("Could not find Ships licence sign at " + vessel.getPosition().getX() + ", " + vessel.getPosition().getY()
                      + ", " + vessel.getPosition().getZ() + ", " + vessel.getPosition().getWorld().getName() + ": "
                      + (vessel instanceof FileBasedVessel ? ((FileBasedVessel) vessel).getFile().getPath() : ""));
    }
}
