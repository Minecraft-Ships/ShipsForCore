package org.ships.vessel.sign;

import org.core.adventureText.AText;
import org.core.entity.living.human.player.LivePlayer;
import org.core.utils.Identifiable;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public interface ShipsSign extends Identifiable {

    boolean isSign(List<? extends AText> lines);

    SignTileEntitySnapshot changeInto(@NotNull SignTileEntity sign) throws IOException;

    boolean onPrimaryClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position);

    boolean onSecondClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position);

    default boolean isSign(@NotNull SignTileEntity entity) {
        return this.isSign(entity.getText());
    }

}
