package org.ships.commands.argument.arguments.identifiable.shiptype;

import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentResult;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.config.ConfigurationNode;
import org.core.config.parser.StringParser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class ShipTypeSingleValueArgument<T> implements CommandArgument<T> {

    private final String id;
    private final BiFunction<? super CommandContext, ? super CommandArgumentContext<T>, ?
            extends ConfigurationNode.KnownParser.SingleKnown<T>> function;

    public ShipTypeSingleValueArgument(String id,
            BiFunction<? super CommandContext, ? super CommandArgumentContext<T>, ?
                    extends ConfigurationNode.KnownParser.SingleKnown<T>> function) {
        this.id = id;
        this.function = function;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public CommandArgumentResult<T> parse(CommandContext context, CommandArgumentContext<T> argument) throws
            IOException {
        String arg = context.getCommand()[argument.getFirstArgument()];
        ConfigurationNode.KnownParser.SingleKnown<T> node = this.function.apply(context, argument);
        Optional<T> opValue = node.getParser().parse(arg);
        if (opValue.isPresent()) {
            return CommandArgumentResult.from(argument, opValue.get());
        }
        throw new IOException("Unknown value of '" + arg + "'");
    }

    @Override
    public List<String> suggest(CommandContext context, CommandArgumentContext<T> argument) {
        String arg = context.getCommand()[argument.getFirstArgument()];
        ConfigurationNode.KnownParser<String, T> node = this.function.apply(context, argument);
        if (!(node.getParser() instanceof StringParser.Suggestible)) {
            return Collections.emptyList();
        }
        return ((StringParser.Suggestible<T>) node.getParser()).getStringSuggestions(arg);
    }
}
