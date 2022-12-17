package org.ships.commands.argument.arguments.identifiable.shiptype;

import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentResult;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.config.ConfigurationNode;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ShipTypeSingleKeyArgument implements CommandArgument<ConfigurationNode.KnownParser.SingleKnown<Object>> {

    public static final Set<ConfigurationNode.KnownParser.SingleKnown<?>> PARSE_FUNCTIONS = new HashSet<>(
            Arrays.asList(AbstractShipType.ALTITUDE_SPEED, AbstractShipType.FUEL_CONSUMPTION, AbstractShipType.MAX_SIZE,
                          AbstractShipType.MAX_SPEED, AbstractShipType.MIN_SIZE, AbstractShipType.FUEL_CONSUMPTION,
                          AbstractShipType.BURNER_BLOCK, AbstractShipType.FUEL_SLOT,
                          AbstractShipType.SPECIAL_BLOCK_PERCENT));
    private final String id;


    public ShipTypeSingleKeyArgument(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public CommandArgumentResult<ConfigurationNode.KnownParser.SingleKnown<Object>> parse(CommandContext context,
                                                                                          CommandArgumentContext<ConfigurationNode.KnownParser.SingleKnown<Object>> argument)
            throws IOException {
        String arg = context.getCommand()[argument.getFirstArgument()];
        Optional<ConfigurationNode.KnownParser.SingleKnown<?>> opNode = PARSE_FUNCTIONS
                .parallelStream()
                .filter(f -> String.join(".", f.getPath()).equalsIgnoreCase(arg))
                .findAny();
        if (opNode.isPresent()) {
            return CommandArgumentResult.from(argument,
                                              (ConfigurationNode.KnownParser.SingleKnown<Object>) opNode.get());
        }
        throw new IOException("Unknown node of " + arg);
    }

    @Override
    public List<String> suggest(CommandContext context,
                                CommandArgumentContext<ConfigurationNode.KnownParser.SingleKnown<Object>> argument) {
        String arg = context.getCommand()[argument.getFirstArgument()];
        return PARSE_FUNCTIONS
                .parallelStream()
                .map(f -> String.join(".", f.getPath()))
                .filter(f -> f.toLowerCase().startsWith(arg.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());

    }
}
