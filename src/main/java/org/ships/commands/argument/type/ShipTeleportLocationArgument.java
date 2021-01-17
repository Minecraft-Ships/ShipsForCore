package org.ships.commands.argument.type;

import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.ships.vessel.common.assits.TeleportToVessel;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ShipTeleportLocationArgument implements CommandArgument<String> {

    private final String id;
    private final OptionalArgument.Parser<TeleportToVessel> toVessel;

    public ShipTeleportLocationArgument(String id, OptionalArgument.Parser<TeleportToVessel> toVessel){
        this.id = id;
        this.toVessel = toVessel;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Map.Entry<String, Integer> parse(CommandContext context, CommandArgumentContext<String> argument) throws IOException {
        TeleportToVessel vessel = this.toVessel.parse(context, new CommandArgumentContext<>(null, argument.getFirstArgument(), context.getCommand()));
        Set<String> keys = vessel.getTeleportPositions().keySet();
        Optional<String> opKey = keys.stream().filter(k -> k.equals(context.getCommand()[argument.getFirstArgument()])).findFirst();
        if(opKey.isPresent()){
            return new AbstractMap.SimpleImmutableEntry<>(opKey.get(), argument.getFirstArgument() + 1);
        }
        throw new IOException("Invalid teleport position of '" + context.getCommand()[argument.getFirstArgument()] + "'");
    }

    @Override
    public List<String> suggest(CommandContext commandContext, CommandArgumentContext<String> argument) {
        TeleportToVessel vessel = this.toVessel.parse(commandContext, new CommandArgumentContext<>(null, argument.getFirstArgument(), commandContext.getCommand()));
        return vessel.getTeleportPositions().keySet().stream().filter(k -> k.toLowerCase().startsWith(commandContext.getCommand()[argument.getFirstArgument()])).collect(Collectors.toList());
    }

    public static ShipTeleportLocationArgument fromArgumentAt(String id, CommandArgument<TeleportToVessel> argument, int position){
        return new ShipTeleportLocationArgument(id, (context, argumentContext) -> {
            try {
                return argument.parse(context, new CommandArgumentContext<>(argument, position, context.getCommand())).getKey();
            } catch (IOException e) {
                throw new IllegalStateException("The argument specified must be before this argument. '" + context.getCommand()[position] + "' is not a valid ship", e);
            }
        });
    }
}
