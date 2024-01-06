package org.ships.commands.argument.arguments.structure;

import org.core.TranslateCore;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentResult;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.world.structure.Structure;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShipsStructureArgument implements CommandArgument<Structure> {

    private final String id;

    public ShipsStructureArgument(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public CommandArgumentResult<Structure> parse(CommandContext context, CommandArgumentContext<Structure> argument)
            throws IOException {
        String t = argument.getFocusArgument();
        Optional<Structure> opStructure = TranslateCore
                .getPlatform()
                .getStructures()
                .parallelStream()
                .filter(structure -> structure.getId().isPresent())
                .filter(structure -> structure.getPlugin().isPresent())
                .filter(structure -> t.equalsIgnoreCase(
                        structure.getPlugin().get().getPluginId() + ":" + structure.getId().get()))
                .findAny();
        if (opStructure.isEmpty()) {
            throw new IOException("Unknown structure");
        }
        return CommandArgumentResult.from(argument, opStructure.get());
    }

    @Override
    public Collection<String> suggest(CommandContext commandContext, CommandArgumentContext<Structure> argument) {
        String peek = argument.getFocusArgument();
        return TranslateCore
                .getPlatform()
                .getStructures()
                .parallelStream()
                .filter(structure -> structure.getId().isPresent())
                .filter(structure -> structure.getPlugin().isPresent())
                .map(structure -> structure.getPlugin().get().getPluginId() + ":" + structure.getId().get())
                .filter(id -> id.toLowerCase().startsWith(peek.toLowerCase()))
                .collect(Collectors.toSet());
    }
}
