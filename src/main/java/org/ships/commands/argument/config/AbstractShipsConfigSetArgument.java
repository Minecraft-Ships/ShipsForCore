package org.ships.commands.argument.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.config.ConfigurationNode;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.ships.commands.argument.arguments.config.ConfigKeyArgument;
import org.ships.commands.argument.arguments.config.ConfigKeyValueArgument;
import org.ships.config.Config;
import org.ships.config.node.DedicatedNode;
import org.ships.permissions.Permissions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class AbstractShipsConfigSetArgument implements ArgumentCommand {

    private static final String COMMAND_NAME = "cmd_name_config";
    private static final String CONFIG_TYPE = "config_type";
    private static final String CONFIG_KEY = "config_key";
    private static final String CONFIG_VALUE = "config_value";

    private final Supplier<? extends Config.KnownNodes> config;
    private final String[] configNames;

    public AbstractShipsConfigSetArgument(Supplier<? extends Config.KnownNodes> config, String... configNames) {
        if (configNames.length == 0) {
            throw new IllegalArgumentException("configNames must have at least one value");
        }
        this.config = config;
        this.configNames = configNames;
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(COMMAND_NAME, false, "config"), new ExactArgument("set"),
                             new ExactArgument(CONFIG_TYPE, false, this.configNames),
                             new ConfigKeyArgument<>(CONFIG_KEY, this.config.get()),
                             new ConfigKeyValueArgument<>(CONFIG_VALUE, (context, argument) -> context.getArgument(this,
                                                                                                                   CONFIG_KEY)));
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
        CommandSource viewer = context.getSource();
        DedicatedNode<?, ?, ? extends ConfigurationNode.KnownParser<String, ?>> node = context.getArgument(this,
                                                                                                           CONFIG_KEY);
        Object argument = context.getArgument(this, CONFIG_VALUE);
        try {
            this.setNode(context);
            viewer.sendMessage(Component.text(node.getKeyName()).color(NamedTextColor.AQUA),
                               Component.text(" as \"" + argument + "\""));
        } catch (IOException e) {
            viewer.sendMessage(Component.text("Failed to set value: " + e.getMessage()));
        }
        return true;
    }

    private <T> void setNode(CommandContext context) throws IOException {
        DedicatedNode<T, T, ? extends ConfigurationNode.KnownParser<String, T>> node = context.getArgument(this,
                                                                                                           CONFIG_KEY);
        T argument = context.getArgument(this, CONFIG_VALUE);

        Config.KnownNodes config = this.config.get();
        node.apply(config.getFile(), argument);
        config.getFile().save();
    }
}
