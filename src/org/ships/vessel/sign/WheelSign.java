package org.ships.vessel.sign;

import org.core.text.TextColours;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;

import java.io.IOException;

public class WheelSign implements ShipsSign {
    @Override
    public boolean isSign(SignTileEntity entity) {
        return entity.getLine(0).equals(TextColours.YELLOW + "[Wheel]");
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) throws IOException {
        SignTileEntitySnapshot stes = sign.getSnapshot();
        stes.setLine(0, TextColours.YELLOW + "[Wheel]");
        stes.setLine(1, TextColours.RED + "\\\\||//");
        stes.setLine(2, TextColours.RED + "==||==");
        stes.setLine(3, TextColours.RED + "//||\\\\");
        return stes;
    }

    @Override
    public String getId() {
        return "ships:wheel_sign";
    }

    @Override
    public String getName() {
        return "Wheel Sign";
    }
}
