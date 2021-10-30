package org.ships.vessel.sign;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.entity.living.human.player.LivePlayer;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class WheelSign implements ShipsSign {

    public final List<AText> SIGN = Arrays.asList(
            AText.ofPlain("[Wheel]").withColour(NamedTextColours.YELLOW),
            AText.ofPlain("\\\\||//").withColour(NamedTextColours.RED),
            AText.ofPlain("==||==").withColour(NamedTextColours.RED),
            AText.ofPlain("//||\\\\").withColour(NamedTextColours.RED)
    );

    @Override
    public boolean isSign(List<? extends AText> lines) {
        return lines.size() >= 1 && lines.get(0).equalsIgnoreCase(this.SIGN.get(0));

    }

    @Override
    public SignTileEntitySnapshot changeInto(@NotNull SignTileEntity sign) {
        SignTileEntitySnapshot stes = sign.getSnapshot();
        stes.setText(this.SIGN);
        return stes;
    }

    @Override
    public boolean onPrimaryClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position) {
        return this.onClick(player, position, true);
    }

    @Override
    public boolean onSecondClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position) {
        return this.onClick(player, position, false);
    }

    @Override
    public String getId() {
        return "ships:wheel_sign";
    }

    @Override
    public String getName() {
        return "Wheel Sign";
    }

    private boolean onClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position, boolean left) {
        if (player.isSneaking()) {
            return false;
        }
        SignUtil.onMovement(position, player, ((context, vessel, exception) -> {
            if (left) {
                vessel.rotateLeftAround(vessel.getPosition(), context, exception);
            } else {
                vessel.rotateRightAround(vessel.getPosition(), context, exception);
            }
        }));
        return false;
    }
}
