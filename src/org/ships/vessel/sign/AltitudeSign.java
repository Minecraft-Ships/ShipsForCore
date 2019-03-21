package org.ships.vessel.sign;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.text.Text;
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
import java.util.Optional;

public class AltitudeSign implements ShipsSign {

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
        stes.setLine(0, CorePlugin.buildText(TextColours.YELLOW + "[Altitude]"));
        stes.setLine(1, CorePlugin.buildText("{Increase}"));
        stes.setLine(2, CorePlugin.buildText("decrease"));
        stes.setLine(3, CorePlugin.buildText("1"));
        return stes;
    }

    @Override
    public Text getFirstLine() {
        return CorePlugin.buildText(TextColours.YELLOW + "[Altitude]");
    }

    @Override
    public boolean onSecondClick(LivePlayer player, BlockPosition position) {
        TileEntitySnapshot tes = ((TiledBlockDetails) position.getBlockDetails()).getTileEntity();
        if(!(tes instanceof SignTileEntity)){
            return false;
        }
        SignTileEntity ste = (SignTileEntity) tes;
        String line1 = ste.getLine(1).get().toPlain();
        String line3 = ste.getLine(3).get().toPlain();
        int altitude = Integer.parseInt(line3);
        try {
            Vessel vessel = new ShipsBlockLoader(position).load();
            if(!(vessel instanceof ShipsVessel)){
                return false;
            }
            ShipsVessel vessel2 = (ShipsVessel)vessel;
            if(line1.startsWith("{")) {
                vessel2.moveTowards(0, altitude, 0, ShipsPlugin.getPlugin().getConfig().getDefaultMovement()).ifPresent(f -> f.sendMessage(player, f.getValue().orElse(null)));
            }else{
                vessel2.moveTowards(0, -altitude, 0, ShipsPlugin.getPlugin().getConfig().getDefaultMovement()).ifPresent(f -> f.sendMessage(player, f.getValue().orElse(null)));

            }
        } catch (IOException e) {
            player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
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
