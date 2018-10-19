package org.ships.vessel.sign;

import org.core.utils.Identifable;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;

import java.io.IOException;

public interface ShipsSign extends Identifable {

    public boolean isSign(SignTileEntity entity);
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) throws IOException;
}
