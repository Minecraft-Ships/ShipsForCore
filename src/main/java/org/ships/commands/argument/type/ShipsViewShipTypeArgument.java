package org.ships.commands.argument.type;

import org.core.adventureText.AText;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;

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
        CommandSource source = commandContext.getSource();
        if (!(source instanceof CommandViewer)) {
            return false;
        }
        CommandViewer viewer = (CommandViewer) source;
        Collection<ShipType<?>> types = ShipsPlugin.getPlugin().getAllShipTypes();
        types.forEach(st -> viewer.sendMessage(AText.ofPlain(" - " + st.getDisplayName())));
        return true;
    }
}
