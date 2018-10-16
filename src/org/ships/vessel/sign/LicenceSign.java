package org.ships.vessel.sign;

import org.core.text.TextColours;
import org.core.world.position.block.entity.sign.SignTileEntity;

public class LicenceSign implements ShipsSign {
    @Override
    public boolean isSign(SignTileEntity entity) {
        return entity.getLine(0).equals(TextColours.YELLOW + "[Ships]");
    }

    @Override
    public String getId() {
        return "ships:licence_sign";
    }

    @Override
    public String getName() {
        return "Licence sign";
    }
}
