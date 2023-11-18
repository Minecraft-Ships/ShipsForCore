package org.ships.commands.argument.arguments.config;

import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentResult;
import org.core.command.argument.ParseCommandArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.config.ConfigurationNode;
import org.core.config.parser.Parser;
import org.core.config.parser.StringParser;
import org.core.exceptions.NotEnoughArguments;
import org.ships.config.node.DedicatedNode;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ConfigKeyValueArgument<A, V, N extends ConfigurationNode.KnownParser<String, V>>
        implements CommandArgument<V> {

    private final String id;
    private final ParseCommandArgument<? extends DedicatedNode<A, V, N>> function;

    public ConfigKeyValueArgument(String id, ParseCommandArgument<? extends DedicatedNode<A, V, N>> supplier) {
        this.id = id;
        this.function = supplier;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public CommandArgumentResult<V> parse(CommandContext context, CommandArgumentContext<V> argument)
            throws IOException {
        String arg = context.getCommand()[argument.getFirstArgument()];
        DedicatedNode<A, V, N> node = this.function
                .parse(context, new CommandArgumentContext<>(null, argument.getFirstArgument(), context.getCommand()))
                .getValue();
        Optional<V> opValue = node.getNode().getParser().parse(arg);
        if (opValue.isPresent()) {
            return CommandArgumentResult.from(argument, opValue.get());
        }
        throw new IOException("Unable to use value of " + arg);
    }

    @Override
    public List<String> suggest(CommandContext context, CommandArgumentContext<V> argument) throws NotEnoughArguments {
        String arg = context.getCommand()[argument.getFirstArgument()];
        DedicatedNode<A, V, N> node = null;
        try {
            node = this.function
                    .parse(context,
                           new CommandArgumentContext<>(null, argument.getFirstArgument(), context.getCommand()))
                    .getValue();
        } catch (IOException e) {
            return Collections.emptyList();
        }
        Parser<String, V> parser = node.getNode().getParser();
        if (!(parser instanceof StringParser.Suggestible<V> sugParser)) {
            return Collections.emptyList();
        }
        return sugParser.getStringSuggestions(arg);
    }
}
