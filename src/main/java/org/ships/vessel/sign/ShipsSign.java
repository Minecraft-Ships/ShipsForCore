package org.ships.vessel.sign;

import org.core.entity.living.human.player.LivePlayer;
import org.core.text.Text;
import org.core.utils.Identifable;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public interface ShipsSign extends Identifable {

    Set<BlockPosition> LOCKED_SIGNS = new HashSet<>();

    boolean isSign(SignTileEntity entity);
    SignTileEntitySnapshot changeInto(SignTileEntity sign) throws IOException;
    Text getFirstLine();

    boolean onPrimaryClick(LivePlayer player, SyncBlockPosition position);
    boolean onSecondClick(LivePlayer player, SyncBlockPosition position);
}
