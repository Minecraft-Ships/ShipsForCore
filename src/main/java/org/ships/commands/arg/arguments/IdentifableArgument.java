package org.ships.commands.arg.arguments;

import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandContext;
import org.core.source.command.CommandSource;
import org.core.utils.Identifable;
import org.ships.plugin.ShipsPlugin;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class IdentifableArgument<I extends Identifable> implements CommandArgument<I> {

    private Class<I> identifable;
    private String id;

    public IdentifableArgument(String id, Class<I> identifable){
        this.id = id;
        this.identifable = identifable;
    }

    @Override
    public Set<String> getSuggestions(CommandSource source, int index, String... words) {
        Set<I> ids = ShipsPlugin.getPlugin().getAll(this.identifable);
        Set<String> toString = new HashSet<>();
        ids.stream().filter(id -> id.getId().toLowerCase().startsWith(words[index].toLowerCase())).forEach(id -> toString.add(id.getId()));
        return toString;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public CommandContext.CommandArgumentEntry<I> run(CommandContext context, CommandSource source, int index, String... words) throws IOException {
        Optional<I> opId = ShipsPlugin.getPlugin().getAll(this.identifable).stream().filter(id -> id.getId().equalsIgnoreCase(words[index])).findAny();
        if(opId.isPresent()){
            return new CommandContext.CommandArgumentEntry<>(this, index, index + 1, opId.get());
        }
        throw new IOException("Unknown " + this.id + " of " + words[index]);
    }
}
