package org.ships.commands.argument.arguments.config;

import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.config.ConfigurationNode;
import org.core.config.parser.Parser;
import org.core.config.parser.StringParser;
import org.ships.config.node.DedicatedNode;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ConfigKeyValueArgument <A, V, N extends ConfigurationNode.KnownParser<String, V>> implements CommandArgument<V> {

    private final String id;
    private final BiFunction<CommandContext, CommandArgumentContext<V>, DedicatedNode<A, V, N>> function;

    public ConfigKeyValueArgument(String id, BiFunction<CommandContext, CommandArgumentContext<V>, DedicatedNode<A, V, N>> supplier){
        this.id = id;
        this.function = supplier;
    }
    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Map.Entry<V, Integer> parse(CommandContext context, CommandArgumentContext<V> argument) throws IOException {
        String arg = context.getCommand()[argument.getFirstArgument()];
        DedicatedNode<A, V, N> node = this.function.apply(context, argument);
        if(node == null){
            throw new IOException("Unknown Config Node");
        }
        Optional<V> opValue = node.getNode().getParser().parse(arg);
        int number = argument.getFirstArgument() + 1;

        if(opValue.isPresent()){
            return new AbstractMap.SimpleImmutableEntry<>(opValue.get(), number);
        }
        throw new IOException("Unable to use value of " + arg);
    }

    @Override
    public List<String> suggest(CommandContext context, CommandArgumentContext<V> argument) {
        String arg = context.getCommand()[argument.getFirstArgument()];
        DedicatedNode<A, V, N> node = this.function.apply(context, argument);
        if(node == null){
            return Collections.emptyList();
        }
        Parser<String, V> parser = node.getNode().getParser();
        if(!(parser instanceof StringParser.Suggestible)){
            return Collections.emptyList();
        }
        StringParser.Suggestible<V> sugParser = (StringParser.Suggestible<V>) parser;
        return sugParser.getStringSuggestions(arg);
    }
}
