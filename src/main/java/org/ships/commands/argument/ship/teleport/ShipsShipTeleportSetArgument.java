package org.ships.commands.argument.ship.teleport;

import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.commands.argument.arguments.ShipTeleportLocationArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.assits.TeleportToVessel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShipsShipTeleportSetArgument implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_TELEPORT_ARGUMENT = "teleport";
    private final String SHIP_SET = "set";
    private final String SHIP_LOCATION = "location";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_ARGUMENT), new ShipIdArgument<>(SHIP_ID_ARGUMENT, v -> v instanceof TeleportToVessel, v -> "Ship is not teleport capable"), new ExactArgument(SHIP_TELEPORT_ARGUMENT), new ExactArgument(SHIP_SET), new OptionalArgument<>(ShipTeleportLocationArgument.fromArgumentAt(SHIP_LOCATION, new ShipIdArgument<>(SHIP_ID_ARGUMENT, v -> v instanceof TeleportToVessel, v -> "Ship is not teleport capable"), 1), "Default"));

    }

    @Override
    public String getDescription() {
        return "Sets a teleport position";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIP_TELEPORT_SET);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource source = commandContext.getSource();
        if (!(source instanceof LivePlayer)) {
            if (source instanceof CommandViewer) {
                ((CommandViewer) source).sendMessagePlain("Teleport requires to be ran as a player");
            }
            return false;
        }
        LivePlayer player = (LivePlayer) source;
        TeleportToVessel tVessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);
        tVessel.setTeleportPosition(player.getPosition(), commandContext.getArgument(this, SHIP_LOCATION));
        tVessel.save();
        return true;
    }
}
