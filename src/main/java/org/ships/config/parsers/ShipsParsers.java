package org.ships.config.parsers;

import org.ships.config.parsers.identify.StringToBlockFinder;
import org.ships.config.parsers.identify.StringToCrewPermission;
import org.ships.config.parsers.identify.StringToMovement;
import org.ships.config.parsers.identify.StringToShipTypeParser;

public interface ShipsParsers {

    NodeToBlockInstruction NODE_TO_BLOCK_INSTRUCTION = new NodeToBlockInstruction();
    StringToCollideTypeParser STRING_TO_COLLIDE_TYPE = new StringToCollideTypeParser();
    StringToShipTypeParser STRING_TO_SHIP_TYPE = new StringToShipTypeParser();
    StringToShipTypeParser.StringToHostCloneableShipTypeParser STRING_TO_HOST_CLONEABLE_SHIP_TYPE =
            new StringToShipTypeParser.StringToHostCloneableShipTypeParser();
    StringToBlockFinder STRING_TO_BLOCK_FINDER = new StringToBlockFinder();
    StringToMovement STRING_TO_MOVEMENT = new StringToMovement();
    StringToCrewPermission STRING_TO_CREW_PERMISSION = new StringToCrewPermission();
}
