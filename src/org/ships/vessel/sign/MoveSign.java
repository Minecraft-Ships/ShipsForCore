package org.ships.vessel.sign;

import org.core.entity.living.human.player.LivePlayer;
import org.core.text.TextColours;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsBlockLoader;
import org.ships.vessel.common.types.ShipsVessel;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;

public class MoveSign implements ShipsSign {

    @Override
    public boolean isSign(SignTileEntity entity) {
        return entity.getLine(0).equals(TextColours.YELLOW + "[Move]");
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) throws IOException {
        SignTileEntitySnapshot stes = sign.getSnapshot();
        stes.setLine(0, TextColours.YELLOW + "[Move]");
        stes.setLine(1, "{Engine}");
        return stes;
    }

    @Override
    public String getFirstLine() {
        return TextColours.YELLOW + "[Move]";
    }

    @Override
    public boolean onSecondClick(LivePlayer player, BlockPosition position) {
        try {
            Vessel vessel = new ShipsBlockLoader(position).load();
            if(!(vessel instanceof ShipsVessel)){
                return false;
            }
            ((ShipsVessel) vessel).rotateLeftAround(vessel.getPosition(), ShipsPlugin.getPlugin().getConfig().getDefaultMovement()).ifPresent(f -> f.sendMessage(player, null));
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    @Override
    public String getId() {
        return "ships:move_sign";
    }

    @Override
    public String getName() {
        return "Move sign";
    }
}
