package org.ships.vessel.sign;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.text.Text;
import org.core.text.TextColours;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;

import java.io.IOException;
import java.util.Optional;

public class WheelSign implements ShipsSign {

    @Override
    public boolean isSign(SignTileEntity entity) {
        Optional<Text> opValue = entity.getLine(0);
        if(opValue.isPresent() && opValue.get().equals(getFirstLine())){
            return true;
        }
        return false;
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) throws IOException {
        SignTileEntitySnapshot stes = sign.getSnapshot();
        stes.setLine(0, CorePlugin.buildText(TextColours.YELLOW + "[Wheel]"));
        stes.setLine(1, CorePlugin.buildText(TextColours.RED + "\\\\||//"));
        stes.setLine(2, CorePlugin.buildText(TextColours.RED + "==||=="));
        stes.setLine(3, CorePlugin.buildText(TextColours.RED + "//||\\\\"));
        return stes;
    }

    @Override
    public Text getFirstLine() {
        return CorePlugin.buildText(TextColours.YELLOW + "[Wheel]");
    }

    @Override
    public boolean onSecondClick(LivePlayer player, BlockPosition position) {
        return false;
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
