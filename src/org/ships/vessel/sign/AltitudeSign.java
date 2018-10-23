package org.ships.vessel.sign;

import org.core.entity.living.human.player.LivePlayer;
import org.core.text.TextColours;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.details.TiledBlockDetails;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsBlockLoader;
import org.ships.vessel.common.types.ShipsVessel;
import org.ships.vessel.common.types.Vessel;

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
    public String getFirstLine() {
        return TextColours.YELLOW + "[Altitude]";
    }

    @Override
    public boolean onSecondClick(LivePlayer player, BlockPosition position) {
        TileEntitySnapshot tes = ((TiledBlockDetails) position.getBlockDetails()).getTileEntity();
        if(!(tes instanceof SignTileEntity)){
            return false;
        }
        SignTileEntity ste = (SignTileEntity) tes;
        String line3 = TextColours.stripColours(ste.getLine(2));
        line3 = line3.replace("{", "");
        line3 = line3.replace("}", "");
        int altitude = Integer.parseInt(line3);
        try {
            Vessel vessel = new ShipsBlockLoader(position).load();
            if(!(vessel instanceof ShipsVessel)){
                return false;
            }
            ShipsVessel vessel2 = (ShipsVessel)vessel;
            vessel2.moveTowards(0, altitude, 0, ShipsPlugin.getPlugin().getConfig().getDefaultMovement()).ifPresent(f -> f.sendMessage(player, null));

        } catch (IOException e) {
            return false;
        }
        return false;
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
