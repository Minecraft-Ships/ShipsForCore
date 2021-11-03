package org.ships.commands.argument.ship.moveto;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.viewer.CommandViewer;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.result.FailedMovement;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.ShipsSign;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ShipsMoveToRotateArgument implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_MOVE_TO_ARGUMENT = "moveTo";
    private final String SHIP_ROTATE_ARGUMENT = "rotate";
    private final String SHIP_ROTATION_ARGUMENT = "rotation";


    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(
                new ExactArgument(this.SHIP_ARGUMENT),
                new ShipIdArgument<>(this.SHIP_ID_ARGUMENT),
                new ExactArgument(this.SHIP_MOVE_TO_ARGUMENT),
                new ExactArgument(this.SHIP_ROTATE_ARGUMENT),
                new ExactArgument(this.SHIP_ROTATION_ARGUMENT, true, "left", "right")
        );
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
        MovementContext context = new MovementContext();
        BasicMovement movement = ShipsPlugin.getPlugin().getConfig().getDefaultMovement();
        context.setMovement(movement);
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        int trackLimit = config.getDefaultTrackSize();

        if (config.isBossBarVisible()) {
            ServerBossBar bar = TranslateCore.createBossBar();
            if (commandContext.getSource() instanceof LivePlayer) {
                bar.register((LivePlayer) commandContext.getSource());
            }
            bar.setTitle(AText.ofPlain("0 / " + trackLimit));
            context.setBar(bar);
        }

        Consumer<Throwable> exceptionSupplier = (exc) -> {
            ShipsSign.LOCKED_SIGNS.remove(position);
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            if (exc instanceof MoveException) {
                MoveException e = (MoveException) exc;
                if (commandContext.getSource() instanceof CommandViewer) {
                    CommandViewer viewer = (CommandViewer) commandContext.getSource();
                    this.sendErrorMessage(viewer, e.getMovement(), e.getMovement().getValue().orElse(null));
                }
            } else {
                exc.printStackTrace();
            }
            context.getEntities().keySet().forEach(s -> {
                if (s instanceof EntitySnapshot.NoneDestructibleSnapshot) {
                    ((EntitySnapshot.NoneDestructibleSnapshot<? extends LiveEntity>) s).getEntity().setGravity(true);
                }
            });
        };

        switch (rotate) {
            case "right":
                vessel.rotateRightAround(position, context, exceptionSupplier);
                break;
            case "left":
                vessel.rotateLeftAround(position, context, exceptionSupplier);
                break;
        }

        return true;
    }

    private <T> void sendErrorMessage(CommandViewer viewer, FailedMovement<T> movement, Object value) {
        movement.sendMessage(viewer, (T) value);
    }
}
