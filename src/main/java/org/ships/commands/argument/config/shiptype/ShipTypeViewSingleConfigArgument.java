package org.ships.commands.argument.config.shiptype;

import org.core.CorePlugin;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.config.ConfigurationNode;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.commands.argument.arguments.identifiable.shiptype.ShipTypeSingleKeyArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.types.ShipType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShipTypeViewSingleConfigArgument implements ArgumentCommand {

    private static final String COMMAND_NAME = "cmd_name_config";
    private static final String CONFIG_TYPE = "config_type";
    private static final String SHIP_TYPE = "ship_type";
    private static final String CONFIG_KEY = "config_key";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(
                new ExactArgument(COMMAND_NAME, false, "config"),
                new ExactArgument("view"),
                new ExactArgument(CONFIG_TYPE, false, "shiptype"),
                new ShipIdentifiableArgument<>(SHIP_TYPE, ShipType.class),
                new ShipTypeSingleKeyArgument(CONFIG_KEY));
    }

    @Override
    public String getDescription() {
        return "View information about the ship type";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_CONFIG_VIEW);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        return runGeneric(commandContext, args);
    }

    private <T> boolean runGeneric(CommandContext commandContext, String... args) {
        if (!(commandContext.getSource() instanceof CommandViewer)) {
            return false;
        }
        CommandViewer viewer = (CommandViewer) commandContext.getSource();
        ShipType<?> type = commandContext.getArgument(this, SHIP_TYPE);
        ConfigurationNode.KnownParser<String, T> parser = commandContext.getArgument(this, CONFIG_KEY);
        Optional<T> opResult = type.getFile().parse(parser);
        if (!opResult.isPresent()) {
            viewer.sendMessagePlain("No value found at node. Is it for this ShipType?");
            return true;
        }
        viewer.sendMessage(CorePlugin.buildText(TextColours.AQUA + "Value is '" + parser.getParser().unparse(opResult.get()) + "'"));
        return true;
    }
}
