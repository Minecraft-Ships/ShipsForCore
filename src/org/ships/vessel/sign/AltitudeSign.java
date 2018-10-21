package org.ships.vessel.sign;

import org.core.text.TextColours;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;

import java.io.IOException;

public class AltitudeSign implements ShipsSign {
    @Override
    public boolean isSign(SignTileEntity entity) {
        return entity.getLine(0).equals(TextColours.YELLOW + "[Altitude]");
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) throws IOException {
        SignTileEntitySnapshot stes = sign.getSnapshot();
        stes.setLine(0, TextColours.YELLOW + "[Altitude]");
        stes.setLine(1, "{Increase}");
        stes.setLine(2, "decrease");
        stes.setLine(3, "1");
        return stes;
    }

    @Override
    public String getId() {
        return "ships:altitude_sign";
    }

    @Override
    public String getName() {
        return "Altitude Sign";
    }
}
