package org.ships.commands.argument.arguments.identifiable.shiptype.flag;

import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentResult;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.utils.Identifiable;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.ShipType;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ShipTypeFlagArgument implements CommandArgument<VesselFlag<?>> {

    private final String id;
    private final BiFunction<? super CommandContext, ? super CommandArgumentContext<VesselFlag<?>>, ? extends ShipType<?>> getter;

    public ShipTypeFlagArgument(String id, BiFunction<? super CommandContext, ? super CommandArgumentContext<VesselFlag<?>>, ? extends ShipType<?>> getter) {
        this.id = id;
        this.getter = getter;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public CommandArgumentResult<VesselFlag<?>> parse(CommandContext context, CommandArgumentContext<VesselFlag<?>> argument) throws IOException {
        String arg = argument.getFocusArgument();
        ShipType<?> type = this
                .getter
                .apply(context, argument);
        VesselFlag<?> flag = type
                .getFlags()
                .stream()
                .filter(vf -> vf.getId().equalsIgnoreCase(arg)).findAny().orElseThrow(() -> new IOException("No VesselFlag with that id in vesseltype of " + type.getDisplayName()));
        return CommandArgumentResult.from(argument, flag);
    }

    @Override
    public List<String> suggest(CommandContext commandContext, CommandArgumentContext<VesselFlag<?>> argument) {
        String arg = argument.getFocusArgument();
        return this
                .getter
                .apply(commandContext, argument)
                .getFlags()
                .stream()
                .map(Identifiable::getId)
                .filter(id -> id.contains(arg))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }
}
