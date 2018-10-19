package org.ships.config.blocks.parsers;

import org.ships.config.blocks.parsers.identify.StringToShipTypeParser;

public interface ShipsParsers {

    NodeToBlockInstruction NODE_TO_BLOCK_INSTRUCTION = new NodeToBlockInstruction();
    StringToCollideTypeParser STRING_TO_COLLIDE_TYPE = new StringToCollideTypeParser();
    StringToShipTypeParser STRING_TO_SHIP_TYPE = new StringToShipTypeParser();
}
