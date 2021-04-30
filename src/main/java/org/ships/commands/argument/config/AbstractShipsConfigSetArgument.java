package org.ships.commands.argument.config;

import org.core.CorePlugin;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.config.ConfigurationNode;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.ships.commands.argument.arguments.config.ConfigKeyArgument;
import org.ships.commands.argument.arguments.config.ConfigKeyValueArgument;
import org.ships.config.Config;
import org.ships.config.node.DedicatedNode;
import org.ships.permissions.Permissions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AbstractShipsConfigSetArgument implements ArgumentCommand {

    private static final String COMMAND_NAME = "cmd_name_config";
    private static final String CONFIG_TYPE = "config_type";
    private static final String CONFIG_KEY = "config_key";
    private static final String CONFIG_VALUE = "config_value";

    private final Config.KnownNodes config;
    private final String[] configNames;

    public AbstractShipsConfigSetArgument(Config.KnownNodes config, String... configNames) {
        if (configNames.length == 0) {
            throw new IllegalArgumentException("configNames must have at least one value");
        }
        this.config = config;
        this.configNames = configNames;
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(
                new ExactArgument(COMMAND_NAME, false, "config"),
                new ExactArgument("set"),
                new ExactArgument(CONFIG_TYPE, false, this.configNames),
                new ConfigKeyArgument<>(CONFIG_KEY, this.config),
                new ConfigKeyValueArgument<>(CONFIG_VALUE, (context, argument) -> context.getArgument(this, CONFIG_KEY)));
    }

    @Override
    public String getDescription() {
        return "View config value";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_CONFIG_VIEW);
    }

    @Override
    public boolean run(CommandContext context, String... args) throws NotEnoughArguments {
        if (!(context.getSource() instanceof CommandViewer)) {
            return false;
        }
        CommandViewer viewer = (CommandViewer) context.getSource();
        DedicatedNode<?, ?, ? extends ConfigurationNode.KnownParser<String, ?>> node = context.getArgument(this, CONFIG_KEY);
        Object argument = context.getArgument(this, CONFIG_VALUE);
        try {
            setNode(context);
            viewer.sendMessage(CorePlugin.buildText("Set " + TextColours.AQUA + "\"" + node.getKeyName() + "\"" + TextColours.RESET + " as \"" + argument + "\""));
        } catch (IOException e) {
            viewer.sendMessage(CorePlugin.buildText("Failed to set value: " + e.getMessage()));
        }
        return true;
    }

    private <T> void setNode(CommandContext context) throws IOException {
        DedicatedNode<T, T, ? extends ConfigurationNode.KnownParser<String, T>> node = context.getArgument(this, CONFIG_KEY);
        T argument = context.getArgument(this, CONFIG_VALUE);

        node.apply(this.config.getFile(), argument);
        this.config.getFile().save();
    }
}
