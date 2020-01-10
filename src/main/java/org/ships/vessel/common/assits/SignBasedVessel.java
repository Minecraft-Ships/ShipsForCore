package org.ships.vessel.common.assits;

import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;

public interface SignBasedVessel extends Vessel {

    /**
     * @return the instanceof the Live sign
     * @throws IOException if the sign can not be grabbed
     */
    LiveSignTileEntity getSign() throws IOException;
}
