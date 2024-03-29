package org.ships.commands.argument.config.shiptype;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.commands.argument.arguments.identifiable.shiptype.ShipTypeSingleKeyArgument;
import org.ships.commands.argument.arguments.identifiable.shiptype.ShipTypeSingleValueArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.types.ShipType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShipTypeSetSingleConfigArgument implements ArgumentCommand {

    private static final String COMMAND_NAME = "cmd_name_config";
    private static final String CONFIG_TYPE = "config_type";
    private static final String SHIP_TYPE = "ship_type";
    private static final String CONFIG_KEY = "config_key";
    private static final String CONFIG_VALUE = "config_value";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(COMMAND_NAME, false, "config"), new ExactArgument("set"),
                             new ExactArgument(CONFIG_TYPE, false, "shiptype"),
                             new ShipIdentifiableArgument<>(SHIP_TYPE, ShipType.class),
                             new ShipTypeSingleKeyArgument(CONFIG_KEY), new ShipTypeSingleValueArgument<>(CONFIG_VALUE,
                                                                                                          (c, a) -> c.getArgument(
                                                                                                                  this,
                                                                                                                  CONFIG_KEY)));
    }

    @Override
    public String getDescription() {
        return "View information about the ship type";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_CONFIG_SET);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        return this.runGeneric(commandContext, args);
    }

    private <T> boolean runGeneric(CommandContext commandContext, String... args) throws NotEnoughArguments {
        ShipType<?> type = commandContext.getArgument(this, SHIP_TYPE);
        ConfigurationNode.KnownParser.SingleKnown<T> parser = commandContext.getArgument(this, CONFIG_KEY);
        T value = commandContext.getArgument(this, CONFIG_VALUE);
        ConfigurationStream.ConfigurationFile file = type.getFile();
        file.set(parser, value);
        file.save();
        AText text = AText.ofPlain("Value has been set").withColour(NamedTextColours.AQUA);
        commandContext.getSource().sendMessage(text);
        return true;
    }
}
