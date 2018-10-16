package org.ships.vessel.sign;

import org.core.utils.Identifable;
import org.core.world.position.block.entity.sign.SignTileEntity;

public interface ShipsSign extends Identifable {

    public boolean isSign(SignTileEntity entity);
}
