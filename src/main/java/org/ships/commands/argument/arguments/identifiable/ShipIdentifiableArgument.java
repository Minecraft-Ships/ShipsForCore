package org.ships.commands.argument.arguments.identifiable;

import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentResult;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.utils.Identifiable;
import org.core.utils.lamda.tri.TriPredicate;
import org.ships.plugin.ShipsPlugin;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShipIdentifiableArgument<T extends Identifiable> implements CommandArgument<T> {

    private final String id;
    private final Class<T> type;
    private final TriPredicate<? super CommandContext, ? super CommandArgumentContext<T>, ? super T> predicate;

    public ShipIdentifiableArgument(String id, Class<T> type) {
        this(id, type, (c, a, v) -> true);
    }

    public ShipIdentifiableArgument(String id, Class<T> type,
            TriPredicate<? super CommandContext, ? super CommandArgumentContext<T>, ? super T> predicate) {
        this.id = id;
        this.type = type;
        this.predicate = predicate;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public CommandArgumentResult<T> parse(CommandContext context, CommandArgumentContext<T> argument) throws
            IOException {
        String arg = context.getCommand()[argument.getFirstArgument()];
        Optional<T> opValue = ShipsPlugin
                .getPlugin()
                .getAll(this.type)
                .parallelStream()
                .filter(i -> i.getId().equalsIgnoreCase(arg))
                .filter(v -> this.predicate.apply(context, argument, v))
                .findAny();
        if (opValue.isPresent()) {
            return CommandArgumentResult.from(argument, opValue.get());
        }
        throw new IOException("Unknown value of '" + arg + "'");
    }

    @Override
    public List<String> suggest(CommandContext context, CommandArgumentContext<T> argument) {
        String arg = context.getCommand()[argument.getFirstArgument()];
        return ShipsPlugin
                .getPlugin()
                .getAll(this.type)
                .parallelStream()
                .filter(i -> i.getId().startsWith(arg.toLowerCase()) || i.getName().startsWith(arg.toLowerCase()))
                .filter(v -> this.predicate.apply(context, argument, v))
                .map(Identifiable::getId)
                .collect(Collectors.toList());
    }
}
