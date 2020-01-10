package org.ships.commands.argument.arguments.ship.type;

import org.core.command.argument.ArgumentContext;
import org.core.command.argument.arguments.generic.SuggestibleParserArgument;
import org.core.configuration.parser.StringParser;
import org.ships.config.parsers.ShipsParsers;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;

import java.io.IOException;

public class HostCloneableShipTypeArgument extends SuggestibleParserArgument<CloneableShipType> {

    public HostCloneableShipTypeArgument(String id) {
        super(id, ShipsParsers.STRING_TO_HOST_CLONABLE_SHIP_TYPE);
    }

    @Override
    protected IOException unableToParse(String next) {
        return new IOException("Unknown host ShipType");
    }
}
