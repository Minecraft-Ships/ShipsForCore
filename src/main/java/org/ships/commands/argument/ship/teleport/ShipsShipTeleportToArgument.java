package org.ships.commands.argument.ship.teleport;

import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.world.position.impl.sync.SyncExactPosition;
import org.ships.commands.argument.type.ShipIdArgument;
import org.ships.commands.argument.type.ShipTeleportLocationArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.assits.TeleportToVessel;

import java.util.Arrays;
import java.util.List;

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
    public String getPermissionNode() {
        return Permissions.CMD_SHIP_TELEPORT.getPermissionValue();
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        if(source instanceof LivePlayer){
            return ((LivePlayer) source).hasPermission(Permissions.CMD_SHIP_TELEPORT);
        }
        return false;
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource source = commandContext.getSource();
        if(!(source instanceof LivePlayer)){
            if(source instanceof CommandViewer){
                ((CommandViewer)source).sendMessagePlain("Teleport requires to be ran as a player");
            }
            return false;
        }
        LivePlayer player = (LivePlayer)source;
        TeleportToVessel tVessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);
        String telPos = commandContext.getArgument(this, SHIP_LOCATION);
        SyncExactPosition position = tVessel.getTeleportPositions().get(telPos);
        player.setPosition(position);
        return true;
    }
}
