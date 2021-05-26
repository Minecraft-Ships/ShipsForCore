package org.ships.commands.argument.ship.teleport;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
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
import org.core.world.position.impl.ExactPosition;
import org.core.world.position.impl.sync.SyncExactPosition;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.commands.argument.arguments.ShipTeleportLocationArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.assits.TeleportToVessel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShipsShipTeleportToArgument implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_TELEPORT_ARGUMENT = "teleport";
    private final String SHIP_LOCATION = "location";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_ARGUMENT), new ShipIdArgument<>(SHIP_ID_ARGUMENT, v -> v instanceof TeleportToVessel, v -> "Ship is not teleport capable"), new ExactArgument(SHIP_TELEPORT_ARGUMENT), new OptionalArgument<>(ShipTeleportLocationArgument.fromArgumentAt(SHIP_LOCATION, new ShipIdArgument<>(SHIP_ID_ARGUMENT, v -> v instanceof TeleportToVessel, v -> "Ship is not teleport capable"), 1), "Default"));
    }

    @Override
    public String getDescription() {
        return "Teleport to a ship";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIP_TELEPORT);
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        if (source instanceof LivePlayer) {
            return ((LivePlayer) source).hasPermission(Permissions.CMD_SHIP_TELEPORT);
        }
        return false;
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
        String telPos = commandContext.getArgument(this, SHIP_LOCATION);
        ExactPosition position = tVessel.getTeleportPositions().get(telPos);
        if (position == null) {
            player.sendMessage(AText.ofPlain("Unknown part of ship").withColour(NamedTextColours.RED));
            return false;
        }
        player.setPosition(position);
        return true;
    }
}
