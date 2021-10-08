package org.ships.permissions;

import org.core.TranslateCore;
import org.core.permission.Permission;
import org.ships.vessel.common.types.ShipType;

public interface Permissions {

    String ABSTRACT_SHIP_MOVE = "ships.move.own";
    String ABSTRACT_SHIP_MOVE_OTHER = "ships.move.other";
    String ABSTRACT_SHIP_MAKE = "ships.make";
    Permission SHIP_REMOVE_OTHER = TranslateCore.getPlatform().register("ships.remove.other");
    Permission CMD_INFO = TranslateCore.getPlatform().register("ships.cmd.info");
    Permission CMD_BLOCK_INFO = TranslateCore.getPlatform().register("ships.cmd.blockinfo");
    Permission CMD_SHIPTYPE_CREATE = TranslateCore.getPlatform().register("ships.cmd.shiptype.create");
    Permission CMD_SHIPTYPE_VIEW_FLAG = TranslateCore.getPlatform().register("ships.cmd.shiptype.flags.view");
    Permission CMD_SHIPTYPE_MODIFY_FLAG = TranslateCore.getPlatform().register("ships.cmd.shiptype.flags.modify");

    Permission CMD_CONFIG_SET = TranslateCore.getPlatform().register("ships.cmd.config.set");
    Permission CMD_CONFIG_VIEW = TranslateCore.getPlatform().register("ships.cmd.config.view");
    Permission CMD_AUTOPILOT = TranslateCore.getPlatform().register("ships.cmd.autopilot");
    Permission CMD_BLOCKLIST_SET = TranslateCore.getPlatform().register("ships.cmd.blocklist.set");
    Permission CMD_BLOCKLIST_VIEW = TranslateCore.getPlatform().register("ships.cmd.blocklist.view");
    Permission CMD_SHIP_TRACK = TranslateCore.getPlatform().register("ships.cmd.ship.track");
    Permission CMD_SHIP_EOT = TranslateCore.getPlatform().register("ships.cmd.ship.eot");
    Permission CMD_SHIP_CREW = TranslateCore.getPlatform().register("ships.cmd.ship.crew");
    Permission CMD_SHIP_MOVETO_POSITION = TranslateCore.getPlatform().register("ships.cmd.ship.moveto.position");
    Permission CMD_SHIP_MOVETO_ROTATE = TranslateCore.getPlatform().register("ships.cmd.ship.moveto.rotate");
    Permission CMD_SHIP_TELEPORT = TranslateCore.getPlatform().register("ships.cmd.ship.teleport");
    Permission CMD_SHIP_TELEPORT_SET = TranslateCore.getPlatform().register("ships.cmd.ship.teleport.set");

    static String getMakePermission(ShipType<?> type) {
        return ABSTRACT_SHIP_MAKE + "." + type.getId().replace(":", ".").toLowerCase();
    }

    static String getMovePermission(ShipType<?> type) {
        return ABSTRACT_SHIP_MOVE + "." + type.getId().replace(":", ".").toLowerCase();
    }

    static String getOtherMovePermission(ShipType<?> type) {
        return ABSTRACT_SHIP_MOVE_OTHER + "." + type.getId().replace(":", ".").toLowerCase();
    }
}
