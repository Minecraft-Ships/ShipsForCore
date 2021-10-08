package org.ships.commands.argument.ship.moveto;

import org.core.TranslateCore;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.SuggestionArgument;
import org.core.command.argument.arguments.position.vector.Vector3IntegerArgument;
import org.core.command.argument.arguments.simple.number.IntegerArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.viewer.CommandViewer;
import org.core.vector.type.Vector3;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.Positionable;
import org.core.world.position.impl.Position;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ShipsMoveToExactArgument implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_MOVE_TO_ARGUMENT = "moveTo";
    private final String SHIP_EXACT_ARGUMENT = "exact";
    private final String SHIP_VECTOR_ARGUMENT = "vector";


    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(
                new ExactArgument(SHIP_ARGUMENT),
                new ShipIdArgument<>(SHIP_ID_ARGUMENT),
                new ExactArgument(SHIP_MOVE_TO_ARGUMENT),
                new ExactArgument(SHIP_EXACT_ARGUMENT),
                new Vector3IntegerArgument(SHIP_VECTOR_ARGUMENT, createSuggestion(p -> p.getX().intValue()), createSuggestion(p -> p.getY().intValue()), createSuggestion(p -> p.getZ().intValue()))
        );
    }

    private SuggestionArgument<Integer> createSuggestion(Function<Position<? extends Number>, Integer> function) {
        return new SuggestionArgument<Integer>(new IntegerArgument(SHIP_VECTOR_ARGUMENT)) {
            @Override
            public List<String> suggest(CommandContext commandContext, CommandArgumentContext<Integer> argument) {
                if (commandContext.getSource() instanceof Positionable) {
                    Positionable<? extends Number> source = (Positionable<? extends Number>) commandContext.getSource();
                    return Collections.singletonList("" + function.apply(source.getPosition()));
                }
                return Collections.emptyList();
            }
        };
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
        Vessel vessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);
        Vector3<Integer> vector3 = commandContext.getArgument(this, SHIP_VECTOR_ARGUMENT);
        SyncBlockPosition position = Position.toSync(Position.toBlock(vessel.getPosition().getWorld().getPosition(vector3)));
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
            bar.setMessage(TranslateCore.buildText("0 / " + trackLimit));
            context.setBar(bar);
        }

        vessel.moveTo(position, context, (exc) -> {
            ShipsSign.LOCKED_SIGNS.remove(position);
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            if (exc instanceof MoveException) {
                MoveException e = (MoveException) exc;
                if (commandContext.getSource() instanceof CommandViewer) {
                    CommandViewer viewer = (CommandViewer) commandContext.getSource();
                    sendErrorMessage(viewer, e.getMovement(), e.getMovement().getValue().orElse(null));
                }
            } else {
                exc.printStackTrace();
            }
            context.getEntities().keySet().forEach(s -> {
                if (s instanceof EntitySnapshot.NoneDestructibleSnapshot) {
                    ((EntitySnapshot.NoneDestructibleSnapshot<? extends LiveEntity>) s).getEntity().setGravity(true);
                }
            });
        });
        return true;
    }

    private <T> void sendErrorMessage(CommandViewer viewer, FailedMovement<T> movement, Object value) {
        movement.sendMessage(viewer, (T) value);
    }
}
