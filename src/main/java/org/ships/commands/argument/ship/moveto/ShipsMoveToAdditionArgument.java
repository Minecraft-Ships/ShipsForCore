package org.ships.commands.argument.ship.moveto;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.position.vector.Vector3IntegerArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.vector.type.Vector3;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.config.configuration.ShipsConfig;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.movement.instruction.details.SimpleMovementException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShipsMoveToAdditionArgument implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_MOVE_TO_ARGUMENT = "moveTo";
    private final String SHIP_ADDITION_ARGUMENT = "add";
    private final String SHIP_VECTOR_ARGUMENT = "vector";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(this.SHIP_ARGUMENT), new ShipIdArgument<>(this.SHIP_ID_ARGUMENT),
                             new ExactArgument(this.SHIP_MOVE_TO_ARGUMENT),
                             new ExactArgument(this.SHIP_ADDITION_ARGUMENT, false, "add", "minus"),
                             new Vector3IntegerArgument(this.SHIP_VECTOR_ARGUMENT));
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
        Vector3<Integer> vector3 = commandContext.getArgument(this, this.SHIP_VECTOR_ARGUMENT);
        if (commandContext.getArgument(this, this.SHIP_ADDITION_ARGUMENT).equals("minus")) {
            vector3 = Vector3.valueOf(-vector3.getX(), -vector3.getY(), -vector3.getZ());
        }
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

        builder.setException(new SimpleMovementException(commandContext.getSource()));

        vessel.moveTowards(vector3, builder.build());

        return true;
    }
}
