package org.ships.vessel.sign;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.text.Text;
import org.core.text.TextColours;
import org.core.vector.types.Vector3Int;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.details.AttachableDetails;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsBlockLoader;
import org.ships.vessel.common.types.ShipsVessel;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;
import java.util.Optional;

public class MoveSign implements ShipsSign {

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
        stes.setLine(0, CorePlugin.buildText(TextColours.YELLOW + "[Move]"));
        stes.setLine(1, CorePlugin.buildText("{Engine}"));
        return stes;
    }

    @Override
    public Text getFirstLine() {
        return CorePlugin.buildText(TextColours.YELLOW + "[Move]");
    }

    @Override
    public boolean onSecondClick(LivePlayer player, BlockPosition position) {
        try {
            Vessel vessel = new ShipsBlockLoader(position).load();
            if(!(vessel instanceof ShipsVessel)){
                System.err.println("Vessel is not ShipsVessel");
                return false;
            }

            Vector3Int direction = ((AttachableDetails)position.getBlockDetails()).getAttachedDirection().getOpposite().getAsVector();
            BasicMovement movement = ShipsPlugin.getPlugin().getConfig().getDefaultMovement();
            ((ShipsVessel) vessel).moveTowards(direction, movement).ifPresent(f -> f.sendMessage(player, f.getValue().orElse(null)));
        } catch (IOException e) {
            player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
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
