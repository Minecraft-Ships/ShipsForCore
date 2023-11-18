package org.ships.commands.argument.arguments.identifiable.shiptype.flag;

import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentResult;
import org.core.command.argument.ParseCommandArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.utils.Identifiable;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.ShipType;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ShipTypeFlagArgument implements CommandArgument<VesselFlag<?>> {

    private final String id;
    private final ParseCommandArgument<? extends ShipType<?>> getter;

    public ShipTypeFlagArgument(String id, ParseCommandArgument<? extends ShipType<?>> getter) {
        this.id = id;
        this.getter = getter;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public CommandArgumentResult<VesselFlag<?>> parse(CommandContext context,
                                                      CommandArgumentContext<VesselFlag<?>> argument)
            throws IOException {
        String arg = argument.getFocusArgument();
        ShipType<?> type = this.getter
                .parse(context, new CommandArgumentContext<>(null, argument.getFirstArgument(), context.getCommand()))
                .getValue();
        VesselFlag<?> flag = type
                .getFlags()
                .stream()
                .filter(vf -> vf.getId().equalsIgnoreCase(arg))
                .findAny()
                .orElseThrow(
                        () -> new IOException("No VesselFlag with that id in vesseltype of " + type.getDisplayName()));
        return CommandArgumentResult.from(argument, flag);
    }

    @Override
    public List<String> suggest(CommandContext commandContext, CommandArgumentContext<VesselFlag<?>> argument) {
        String arg = argument.getFocusArgument();
        try {
            return this.getter
                    .parse(commandContext,
                           new CommandArgumentContext<>(null, argument.getFirstArgument(), commandContext.getCommand()))
                    .getValue()
                    .getFlags()
                    .stream()
                    .map(Identifiable::getId)
                    .filter(id -> id.contains(arg))
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
