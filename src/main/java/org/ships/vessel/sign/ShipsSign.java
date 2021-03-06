package org.ships.vessel.sign;

import org.core.adventureText.AText;
import org.core.entity.living.human.player.LivePlayer;
import org.core.text.Text;
import org.core.utils.Identifiable;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface ShipsSign extends Identifiable {

    Set<BlockPosition> LOCKED_SIGNS = new HashSet<>();

    boolean isSign(List<AText> lines);

    SignTileEntitySnapshot changeInto(@NotNull SignTileEntity sign) throws IOException;

    @Deprecated
    Text getFirstLine();

    boolean onPrimaryClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position);

    boolean onSecondClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position);

    default boolean isSign(@NotNull SignTileEntity entity) {
        return isSign(entity.getText());
    }

}
