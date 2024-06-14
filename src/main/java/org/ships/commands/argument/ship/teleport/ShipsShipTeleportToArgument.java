package org.ships.commands.argument.ship.teleport;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.User;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.world.position.impl.ExactPosition;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.commands.argument.arguments.ShipTeleportLocationArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.assits.CrewStoredVessel;
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
        return Arrays.asList(new ExactArgument(this.SHIP_ARGUMENT),
                             new ShipIdArgument<>(this.SHIP_ID_ARGUMENT, (source, vessel) -> {
                                 if (source instanceof LivePlayer && vessel instanceof CrewStoredVessel) {
                                     User player = (User) source;
                                     return ((CrewStoredVessel) vessel)
                                             .getPermission(player.getUniqueId())
                                             .canCommand();
                                 }
                                 return vessel instanceof TeleportToVessel;
                             }, v -> "Ship is not teleport capable"), new ExactArgument(this.SHIP_TELEPORT_ARGUMENT),
                             new OptionalArgument<>(ShipTeleportLocationArgument.fromArgumentAt(this.SHIP_LOCATION,
                                                                                                new ShipIdArgument<>(
                                                                                                        this.SHIP_ID_ARGUMENT,
                                                                                                        (source, v) -> v instanceof TeleportToVessel,
                                                                                                        v -> "Ship is not teleport capable"),
                                                                                                1), "Default"));
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
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource source = commandContext.getSource();
        if (!(source instanceof LivePlayer)) {
            source.sendMessage(Component.text("Teleport requires to be ran as a player"));
            return false;
        }
        LivePlayer player = (LivePlayer) source;
        TeleportToVessel tVessel = commandContext.getArgument(this, this.SHIP_ID_ARGUMENT);
        String telPos = commandContext.getArgument(this, this.SHIP_LOCATION);
        ExactPosition position = tVessel.getTeleportPositions().get(telPos);
        if (position == null) {
            player.sendMessage(Component.text("Unknown part of ship").color(NamedTextColor.RED));
            return false;
        }
        player.setPosition(position);
        return true;
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        if (source instanceof LivePlayer) {
            return ((LivePlayer) source).hasPermission(Permissions.CMD_SHIP_TELEPORT);
        }
        return false;
    }
}
