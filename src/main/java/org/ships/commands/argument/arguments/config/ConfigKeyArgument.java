package org.ships.commands.argument.arguments.config;

import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.config.ConfigurationNode;
import org.jetbrains.annotations.NotNull;
import org.ships.config.Config;
import org.ships.config.node.DedicatedNode;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConfigKeyArgument<A, V, N extends ConfigurationNode.KnownParser<?, V>> implements CommandArgument<DedicatedNode<A, V, N>> {

    private final @NotNull Config.KnownNodes config;
    private final @NotNull String id;

    public ConfigKeyArgument(@NotNull String id, @NotNull Config.KnownNodes nodes) {
        if (id == null || nodes == null) {
            throw new IllegalArgumentException("Either Id or Nodes is null");
        }
        this.config = nodes;
        this.id = id;
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public Map.Entry<DedicatedNode<A, V, N>, Integer> parse(CommandContext context, CommandArgumentContext<DedicatedNode<A, V, N>> argument) throws IOException {
        String arg = context.getCommand()[argument.getFirstArgument()];
        int number = argument.getFirstArgument() + 1;
        Optional<DedicatedNode<Object, Object, ConfigurationNode.KnownParser<?, Object>>> opNode = this
                .config
                .getNodes()
                .parallelStream()
                .filter(n -> n.getKeyName().equalsIgnoreCase(arg))
                .findAny();
        if (opNode.isPresent()) {
            return new AbstractMap.SimpleImmutableEntry<>((DedicatedNode<A, V, N>) opNode.get(), number);
        }
        throw new IOException("No known node of " + arg);
    }

    @Override
    public List<String> suggest(CommandContext context, CommandArgumentContext<DedicatedNode<A, V, N>> argument) {
        String arg = context.getCommand()[argument.getFirstArgument()];
        return this.config.getNodes().parallelStream().map(DedicatedNode::getKeyName).filter(n -> {
            return n.toLowerCase().startsWith(arg.toLowerCase());
        }).collect(Collectors.toList());
    }
}
