package org.ships.commands.argument.ship.moveto;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.utils.BarUtils;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ShipsMoveToRotateArgument implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_MOVE_TO_ARGUMENT = "moveTo";
    private final String SHIP_ROTATE_ARGUMENT = "rotate";
    private final String SHIP_ROTATION_ARGUMENT = "rotation";


    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(this.SHIP_ARGUMENT), new ShipIdArgument<>(this.SHIP_ID_ARGUMENT),
                             new ExactArgument(this.SHIP_MOVE_TO_ARGUMENT),
                             new ExactArgument(this.SHIP_ROTATE_ARGUMENT),
                             new ExactArgument(this.SHIP_ROTATION_ARGUMENT, true, "left", "right"));
    }

    @Override
    public String getDescription() {
        return "Move the ship to the position";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        Vessel vessel = commandContext.getArgument(this, this.SHIP_ID_ARGUMENT);
        String rotate = commandContext.getArgument(this, this.SHIP_ROTATION_ARGUMENT);
        SyncBlockPosition position = vessel.getPosition();
        MovementDetailsBuilder builder = new MovementDetailsBuilder();
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        int trackLimit = config.getDefaultTrackSize();

        if (config.isBossBarVisible()) {
            BossBar bar = BossBar.bossBar(Component.text("0 / " + trackLimit), 0, BossBar.Color.PURPLE,
                                          BossBar.Overlay.PROGRESS);
            if (commandContext.getSource() instanceof Audience audience) {
                audience.showBossBar(bar);
            }
            builder.setAdventureBossBar(bar);
        }

        BiConsumer<MovementContext, Throwable> exceptionSupplier = (context, exc) -> {
            ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
            context.getAdventureBossBar().ifPresent(bar -> {
                BarUtils.getPlayers(bar).forEach(player -> player.hideBossBar(bar));
            });
            if (exc instanceof MoveException e) {
                commandContext.getSource().sendMessage(e.getErrorMessage());
            } else {
                exc.printStackTrace();
            }
            context
                    .getEntities()
                    .keySet()
                    .stream()
                    .filter(snapshot -> snapshot instanceof EntitySnapshot.NoneDestructibleSnapshot<? extends LiveEntity>)
                    .map(snapshot -> (EntitySnapshot.NoneDestructibleSnapshot<? extends LiveEntity>) snapshot)
                    .forEach(snapshot -> snapshot.getEntity().setGravity(true));
        };

        builder.setException(exceptionSupplier);

        switch (rotate) {
            case "right":
                vessel.rotateRightAround(position, builder.build());
                break;
            case "left":
                vessel.rotateLeftAround(position, builder.build());
                break;
        }

        return true;
    }
}
