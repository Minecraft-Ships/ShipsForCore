package org.ships.commands.argument.arguments;

import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentResult;
import org.core.command.argument.ParseCommandArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.ships.vessel.common.assits.TeleportToVessel;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ShipTeleportLocationArgument implements CommandArgument<String> {

    private final String id;
    private final ParseCommandArgument<TeleportToVessel> toVessel;

    public ShipTeleportLocationArgument(String id, ParseCommandArgument<TeleportToVessel> toVessel) {
        this.id = id;
        this.toVessel = toVessel;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public CommandArgumentResult<String> parse(CommandContext context, CommandArgumentContext<String> argument) throws IOException {
        TeleportToVessel vessel = this.toVessel.parse(context, new CommandArgumentContext<>(null, argument.getFirstArgument(), context.getCommand())).getValue();
        Set<String> keys = vessel.getTeleportPositions().keySet();
        Optional<String> opKey = keys.stream().filter(k -> k.equals(context.getCommand()[argument.getFirstArgument()])).findFirst();
        if (opKey.isPresent()) {
            return CommandArgumentResult.from(argument, opKey.get());
        }
        throw new IOException("Invalid teleport position of '" + context.getCommand()[argument.getFirstArgument()] + "'");
    }

    @Override
    public List<String> suggest(CommandContext commandContext, CommandArgumentContext<String> argument) {
        try {
            TeleportToVessel vessel = this.toVessel.parse(commandContext, new CommandArgumentContext<>(null, argument.getFirstArgument(), commandContext.getCommand())).getValue();
            return vessel.getTeleportPositions().keySet().stream().filter(k -> k.toLowerCase().startsWith(commandContext.getCommand()[argument.getFirstArgument()])).collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static ShipTeleportLocationArgument fromArgumentAt(String id, CommandArgument<TeleportToVessel> argument, int position) {
        return new ShipTeleportLocationArgument(id, (context, argumentContext) -> {
            try {
                return argument.parse(context, new CommandArgumentContext<>(argument, position, context.getCommand()));
            } catch (IOException e) {
                throw new IllegalStateException("The argument specified must be before this argument. '" + context.getCommand()[position] + "' is not a valid ship", e);
            }
        });
    }
}
