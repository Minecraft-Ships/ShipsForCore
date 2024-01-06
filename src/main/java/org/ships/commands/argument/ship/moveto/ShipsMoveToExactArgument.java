package org.ships.commands.argument.ship.moveto;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.arguments.operation.SuggestionArgument;
import org.core.command.argument.arguments.position.WorldArgument;
import org.core.command.argument.arguments.position.vector.Vector3IntegerArgument;
import org.core.command.argument.arguments.simple.number.IntegerArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.utils.BarUtils;
import org.core.vector.type.Vector3;
import org.core.world.WorldExtent;
import org.core.world.position.Positionable;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ShipsMoveToExactArgument implements ArgumentCommand {

    private static final String SHIP_ARGUMENT = "ship";
    private static final String SHIP_ID_ARGUMENT = "ship_id";
    private static final String SHIP_MOVE_TO_ARGUMENT = "moveTo";
    private static final String SHIP_EXACT_ARGUMENT = "exact";
    private static final OptionalArgument<WorldExtent> SHIP_WORLD_ARGUMENT = new OptionalArgument<>(
            new WorldArgument("world"), (WorldExtent) null);
    private static final String SHIP_VECTOR_ARGUMENT = "vector";


    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_ARGUMENT), new ShipIdArgument<>(SHIP_ID_ARGUMENT),
                             new ExactArgument(SHIP_MOVE_TO_ARGUMENT), new ExactArgument(SHIP_EXACT_ARGUMENT),
                             new Vector3IntegerArgument(SHIP_VECTOR_ARGUMENT,
                                                        this.createSuggestion(p -> p.getX().intValue()),
                                                        this.createSuggestion(p -> p.getY().intValue()),
                                                        this.createSuggestion(p -> p.getZ().intValue())),
                             SHIP_WORLD_ARGUMENT

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
        Vessel vessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);
        Vector3<Integer> vector3 = commandContext.getArgument(this, SHIP_VECTOR_ARGUMENT);
        WorldExtent world = commandContext.getArgument(this, SHIP_WORLD_ARGUMENT);
        if (world == null) {
            world = vessel.getPosition().getWorld();
        }

        SyncBlockPosition position = world.getPosition(vector3).toBlockPosition().toSyncPosition();
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
        builder.setException((context, exc) -> {
            ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
            context.getAdventureBossBar().ifPresent(bar -> {
                BarUtils.getPlayers(bar).forEach(user -> user.hideBossBar(bar));
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
        });

        vessel.moveTo(position, builder.build());
        return true;
    }

    private SuggestionArgument<Integer> createSuggestion(Function<? super Position<? extends Number>, Integer> function) {
        return new SuggestionArgument<>(new IntegerArgument(SHIP_VECTOR_ARGUMENT)) {
            @Override
            public List<String> suggest(CommandContext commandContext, CommandArgumentContext<Integer> argument) {
                if (commandContext.getSource() instanceof Positionable) {
                    Positionable<? extends Number> source = (Positionable<? extends Number>) commandContext.getSource();
                    Position<?> pos = source.getPosition();
                    return Collections.singletonList("" + function.apply(pos));
                }
                return Collections.emptyList();
            }
        };
    }
}
