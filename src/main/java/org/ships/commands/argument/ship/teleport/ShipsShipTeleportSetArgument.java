package org.ships.commands.argument.ship.teleport;

import org.core.adventureText.AText;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.Entity;
import org.core.entity.LiveEntity;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.User;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.commands.argument.arguments.ShipTeleportLocationArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.assits.CrewStoredVessel;
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
        return Arrays.asList(
                new ExactArgument(this.SHIP_ARGUMENT),
                new ShipIdArgument<>(this.SHIP_ID_ARGUMENT, (source, vessel) -> {
                    if (source instanceof LivePlayer && vessel instanceof CrewStoredVessel) {
                        CrewStoredVessel crewVessel = (CrewStoredVessel) vessel;
                        User player = (User) source;
                        return crewVessel.getPermission(player.getUniqueId()).canCommand();
                    }
                    return vessel instanceof TeleportToVessel;
                }, v -> "Ship is not teleport capable"),
                new ExactArgument(this.SHIP_TELEPORT_ARGUMENT),
                new ExactArgument(this.SHIP_SET),
                new OptionalArgument<>(ShipTeleportLocationArgument.fromArgumentAt(this.SHIP_LOCATION, new ShipIdArgument<>(this.SHIP_ID_ARGUMENT, (source, v) -> v instanceof TeleportToVessel, v -> "Ship is not teleport capable"), 1), "Default"));

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
                ((CommandViewer) source).sendMessage(AText.ofPlain("Teleport requires to be ran as a player"));
            }
            return false;
        }
        Entity<LiveEntity> player = (Entity<LiveEntity>) source;
        TeleportToVessel tVessel = commandContext.getArgument(this, this.SHIP_ID_ARGUMENT);
        tVessel.setTeleportPosition(player.getPosition(), commandContext.getArgument(this, this.SHIP_LOCATION));
        tVessel.save();
        return true;
    }
}
