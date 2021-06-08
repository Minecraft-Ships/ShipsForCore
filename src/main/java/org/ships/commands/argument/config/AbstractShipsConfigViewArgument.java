package org.ships.commands.argument.config;

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
import org.ships.commands.argument.arguments.config.ConfigKeyArgument;
import org.ships.config.Config;
import org.ships.config.node.DedicatedNode;
import org.ships.permissions.Permissions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class AbstractShipsConfigViewArgument implements ArgumentCommand {

    private static final String COMMAND_NAME = "cmd_name_config";
    private static final String CONFIG_TYPE = "config_type";
    private static final String CONFIG_KEY = "config_key";

    private final Supplier<Config.KnownNodes> config;
    private final String[] configNames;

    public AbstractShipsConfigViewArgument(Supplier<Config.KnownNodes> config, String... configNames) {
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
                new ExactArgument("view"),
                new ExactArgument(CONFIG_TYPE, false, this.configNames),
                new ConfigKeyArgument<>(CONFIG_KEY, this.config.get()));
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
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        if (!(commandContext.getSource() instanceof CommandViewer)) {
            return false;
        }
        CommandViewer viewer = (CommandViewer) commandContext.getSource();
        DedicatedNode<?, ?, ? extends ConfigurationNode.KnownParser<?, ?>> node = commandContext.getArgument(this, CONFIG_KEY);
        String value = readUnknownNode(node);
        viewer.sendMessage(CorePlugin.buildText(TextColours.AQUA + "\"" + node.getKeyName() + "\": " + TextColours.RESET + value));
        return true;
    }

    private <T> String readUnknownNode(DedicatedNode<?, ?, ? extends ConfigurationNode.KnownParser<?, ?>> node) {
        return readNode((DedicatedNode<T, T, ConfigurationNode.KnownParser<String, T>>) node);
    }

    private <T> String readNode(DedicatedNode<T, T, ConfigurationNode.KnownParser<String, T>> node) {
        Optional<T> opValue = this.config.get().getFile().parse(node.getNode());
        if (!opValue.isPresent()) {
            return "<no value>";
        }
        return node.getNode().getParser().unparse(opValue.get());
    }
}
