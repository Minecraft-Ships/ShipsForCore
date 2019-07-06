package org.ships.config.parsers;

import org.ships.config.parsers.identify.StringToBlockFinder;
import org.ships.config.parsers.identify.StringToMovement;
import org.ships.config.parsers.identify.StringToShipTypeParser;
import org.ships.config.parsers.identify.StringToVesselFlag;

public interface ShipsParsers {

    NodeToBlockInstruction NODE_TO_BLOCK_INSTRUCTION = new NodeToBlockInstruction();
    StringToCollideTypeParser STRING_TO_COLLIDE_TYPE = new StringToCollideTypeParser();
    StringToShipTypeParser STRING_TO_SHIP_TYPE = new StringToShipTypeParser();
    StringToBlockFinder STRING_TO_BLOCK_FINDER = new StringToBlockFinder();
    StringToMovement STRING_TO_MOVEMENT = new StringToMovement();
    StringToVesselFlag STRING_TO_VESSEL_FLAG = new StringToVesselFlag();
}
