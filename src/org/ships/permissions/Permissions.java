package org.ships.permissions;

import org.ships.vessel.common.types.ShipType;

public interface Permissions {

    String ABSTRACT_SHIP_MOVE = "ships.move.own";
    String ABSTRACT_SHIP_MOVE_OTHER = "ships.move.other";
    String ABSTRACT_SHIP_MAKE = "ships.make";
    String CMD_INFO  = "ships.cmd.info";

    static String getMakePermission(ShipType type){
        return ABSTRACT_SHIP_MAKE + "." + type.getId().replace(":", ".").toLowerCase();
    }

    static String getMovePermission(ShipType type){
        return ABSTRACT_SHIP_MOVE + "." + type.getId().replace(":", ".").toLowerCase();
    }
}
