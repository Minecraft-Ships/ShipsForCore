package org.ships.vessel.sign;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.core.entity.living.human.player.LivePlayer;
import org.core.utils.ComponentUtils;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class WheelSign implements ShipsSign {

    public static final List<Component> SIGN = List.of(Component.text("[Wheel]").color(NamedTextColor.YELLOW),
                                                       Component.text("\\\\||//").color(TextColor.color(153, 102, 51)),
                                                       Component.text("==||==").color(TextColor.color(153, 102, 51)),
                                                       Component.text("//||\\\\").color(TextColor.color(153, 102, 51)));

    @Override
    public boolean isSign(List<? extends Component> lines) {
        return lines.size() >= 1 && ComponentUtils
                .toPlain(lines.get(0))
                .equalsIgnoreCase(ComponentUtils.toPlain(SIGN.get(0)));

    }

    @Override
    public void changeInto(@NotNull SignSide sign) throws IOException {
        sign.setLines(SIGN);
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
        SignUtil.onMovement(position, player, ((details, vessel) -> {
            if (left) {
                vessel.rotateLeftAround(vessel.getPosition(), details);
                return;
            }
            vessel.rotateRightAround(vessel.getPosition(), details);
        }));
        return true;
    }
}
