package org.ships.vessel.common.assits.shiptype;

import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.vessel.common.types.Vessel;

public interface ClassicShipType {

    Vessel createClassicVessel(SignTileEntity ste, BlockPosition blockPosition);

    default Vessel createClassicVessel(LiveSignTileEntity lste){
        return createClassicVessel(lste, lste.getPosition());
    }
}
