package org.ships.config.parsers;

import org.ships.config.parsers.identify.*;

public interface ShipsParsers {

    NodeToBlockInstruction NODE_TO_BLOCK_INSTRUCTION = new NodeToBlockInstruction();
    StringToCollideTypeParser STRING_TO_COLLIDE_TYPE = new StringToCollideTypeParser();
    StringToShipTypeParser STRING_TO_SHIP_TYPE = new StringToShipTypeParser();
    StringToShipTypeParser.StringToHostClonableShipTypeParser STRING_TO_HOST_CLONABLE_SHIP_TYPE = new StringToShipTypeParser.StringToHostClonableShipTypeParser();
    StringToBlockFinder STRING_TO_BLOCK_FINDER = new StringToBlockFinder();
    StringToMovement STRING_TO_MOVEMENT = new StringToMovement();
    StringToVesselFlag STRING_TO_VESSEL_FLAG = new StringToVesselFlag();
    StringToCrewPermission STRING_TO_CREW_PERMISSION = new StringToCrewPermission();
}
