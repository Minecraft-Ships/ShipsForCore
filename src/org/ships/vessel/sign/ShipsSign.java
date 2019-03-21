package org.ships.vessel.sign;

import org.core.entity.living.human.player.LivePlayer;
import org.core.text.Text;
import org.core.utils.Identifable;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;

import java.io.IOException;

public interface ShipsSign extends Identifable {

    boolean isSign(SignTileEntity entity);
    SignTileEntitySnapshot changeInto(SignTileEntity sign) throws IOException;
    Text getFirstLine();

    boolean onSecondClick(LivePlayer player, BlockPosition position);
}
