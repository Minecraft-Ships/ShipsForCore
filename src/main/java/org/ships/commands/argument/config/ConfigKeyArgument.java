package org.ships.commands.argument.config;

import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.config.ConfigurationNode;
import org.ships.config.Config;
import org.ships.config.node.DedicatedNode;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigKeyArgument <A, V, N extends ConfigurationNode.KnownParser<?, V>> implements CommandArgument<DedicatedNode<A, V, N>> {

    private final Config.KnownNodes config;
    private final String id;

    public ConfigKeyArgument(String id, Config.KnownNodes nodes){
        this.config = nodes;
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Map.Entry<DedicatedNode<A, V, N>, Integer> parse(CommandContext context, CommandArgumentContext<DedicatedNode<A, V, N>> argument) throws IOException {
        String arg = context.getCommand()[argument.getFirstArgument()];
        int number = argument.getFirstArgument() + 1;
        Optional<DedicatedNode<Object, Object, ConfigurationNode.KnownParser<?, Object>>> opNode = this.config.getNodes().parallelStream().filter(n -> n.getKeyName().equalsIgnoreCase(arg)).findAny();
        if(opNode.isPresent()){
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
