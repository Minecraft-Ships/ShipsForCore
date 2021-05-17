package org.ships.commands.argument.arguments;

import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.utils.Identifiable;
import org.ships.plugin.ShipsPlugin;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ShipIdentifiableArgument<T extends Identifiable> implements CommandArgument<T> {

    private final String id;
    private final Class<T> type;
    private final Predicate<T> predicate;

    public ShipIdentifiableArgument(String id, Class<T> type) {
        this(id, type, (v) -> true);
    }

    public ShipIdentifiableArgument(String id, Class<T> type, Predicate<T> predicate) {
        this.id = id;
        this.type = type;
        this.predicate = predicate;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Map.Entry<T, Integer> parse(CommandContext context, CommandArgumentContext<T> argument) throws IOException {
        String arg = context.getCommand()[argument.getFirstArgument()];
        int number = argument.getFirstArgument() + 1;
        Optional<T> opValue = ShipsPlugin
                .getPlugin()
                .getAll(this.type)
                .parallelStream()
                .filter(i -> i.getId().equalsIgnoreCase(arg))
                .filter(this.predicate)
                .findAny();
        if (opValue.isPresent()) {
            return new AbstractMap.SimpleImmutableEntry<>(opValue.get(), number);
        }
        throw new IOException("Unknown value of '" + arg + "'");
    }

    @Override
    public List<String> suggest(CommandContext context, CommandArgumentContext<T> argument) {
        String arg = context.getCommand()[argument.getFirstArgument()];
        int number = argument.getFirstArgument() + 1;
        return ShipsPlugin
                .getPlugin()
                .getAll(this.type)
                .parallelStream()
                .filter(i -> i.getId().startsWith(arg.toLowerCase()) || i.getName().startsWith(arg.toLowerCase()))
                .filter(this.predicate)
                .map(Identifiable::getId)
                .collect(Collectors.toList());
    }
}
