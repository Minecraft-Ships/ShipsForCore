package org.ships.vessel.sign;

import net.kyori.adventure.text.Component;
import org.core.entity.living.human.player.LivePlayer;
import org.core.utils.Identifiable;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ShipsSign extends Identifiable {

    boolean isSign(List<? extends Component> lines);

    void changeInto(@NotNull SignSide sign) throws IOException;

    boolean onPrimaryClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position);

    boolean onSecondClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position);

    default boolean isSign(@NotNull SignTileEntity entity) {
        return this.getSide(entity).isPresent();
    }

    default Optional<SignSide> getSide(SignTileEntity entity) {
        if (this.isSign(entity.getFront().getLines())) {
            return Optional.of(entity.getFront());
        }
        return entity.getBack().filter(side -> this.isSign(side.getLines()));
    }

}
