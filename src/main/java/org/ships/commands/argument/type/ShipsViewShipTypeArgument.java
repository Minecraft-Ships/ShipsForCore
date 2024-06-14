package org.ships.commands.argument.type;

import net.kyori.adventure.text.Component;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipTypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ShipsViewShipTypeArgument implements ArgumentCommand {

    private static final String SHIP_TYPE = "shiptype";
    private static final String VIEW = "view";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_TYPE), new ExactArgument(VIEW));
    }

    @Override
    public String getDescription() {
        return "View all ship types";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource viewer = commandContext.getSource();
        Collection<ShipType<?>> types = ShipTypes.shipTypes();
        types.forEach(st -> viewer.sendMessage(Component.text(" - " + st.getDisplayName())));
        return true;
    }
}
