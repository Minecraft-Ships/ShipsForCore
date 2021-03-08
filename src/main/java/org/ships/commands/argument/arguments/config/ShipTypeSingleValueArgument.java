package org.ships.commands.argument.arguments.config;

import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.config.ConfigurationNode;
import org.core.config.parser.StringParser;
import org.ships.config.node.DedicatedNode;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;

public class ShipTypeSingleValueArgument<T> implements CommandArgument<T> {

    private final String id;
    private final BiFunction<CommandContext, CommandArgumentContext<T>, ConfigurationNode.KnownParser.SingleKnown<T>> function;

    public ShipTypeSingleValueArgument(String id, BiFunction<CommandContext, CommandArgumentContext<T>, ConfigurationNode.KnownParser.SingleKnown<T>> function){
        this.id = id;
        this.function = function;
    }
    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Map.Entry<T, Integer> parse(CommandContext context, CommandArgumentContext<T> argument) throws IOException {
        String arg = context.getCommand()[argument.getFirstArgument()];
        ConfigurationNode.KnownParser.SingleKnown<T> node = this.function.apply(context, argument);
        Optional<T> opValue = node.getParser().parse(arg);
        if(opValue.isPresent()){
            int number = argument.getFirstArgument() + 1;
            return new AbstractMap.SimpleEntry<>(opValue.get(), number);
        }
        throw new IOException("Unknown value of '" + arg + "'");
    }

    @Override
    public List<String> suggest(CommandContext context, CommandArgumentContext<T> argument) {
        String arg = context.getCommand()[argument.getFirstArgument()];
        ConfigurationNode.KnownParser<String, T> node = this.function.apply(context, argument);
        if(!(node.getParser() instanceof StringParser.Suggestible)){
            return Collections.emptyList();
        }
        return ((StringParser.Suggestible<T>) node.getParser()).getStringSuggestions(arg);
    }
}
