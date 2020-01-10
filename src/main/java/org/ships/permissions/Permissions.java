package org.ships.permissions;

import org.ships.vessel.common.types.ShipType;

public interface Permissions {

    String ABSTRACT_SHIP_MOVE = "ships.move.own";
    String ABSTRACT_SHIP_MOVE_OTHER = "ships.move.other";
    String ABSTRACT_SHIP_MAKE = "ships.make";
    String SHIP_REMOVE_OTHER = "ships.remove.other";
    String CMD_INFO  = "ships.cmd.info";
    String CMD_BLOCK_INFO = "ships.cmd.blockinfo";
    String CMD_SHIPTYPE_CREATE = "ships.cmd.shiptype.create";
    String CMD_CONFIG_SET = "ships.cmd.config.set";
    String CMD_CONFIG_VIEW = "ships.cmd.config.view";
    String CMD_AUTOPILOT = "ships.cmd.autopilot";
    String CMD_BLOCKLIST_SET = "ships.cmd.blocklist.set";
    String CMD_BLOCKLIST_VIEW = "ships.cmd.blocklist.view";
    String CMD_SHIP_TRACK = "ships.cmd.ship.track";
    String CMD_SHIP_EOT = "ships.cmd.ship.eot";
    String CMD_SHIP_CREW = "ships.cmd.ship.crew";

    static String getMakePermission(ShipType type){
        return ABSTRACT_SHIP_MAKE + "." + type.getId().replace(":", ".").toLowerCase();
    }

    static String getMovePermission(ShipType type){
        return ABSTRACT_SHIP_MOVE + "." + type.getId().replace(":", ".").toLowerCase();
    }

    static String getOtherMovePermission(ShipType type){
        return ABSTRACT_SHIP_MOVE_OTHER + "." + type.getId().replace(":", ".").toLowerCase();
    }
}
