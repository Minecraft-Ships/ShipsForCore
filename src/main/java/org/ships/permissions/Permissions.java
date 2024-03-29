package org.ships.permissions;

import org.core.TranslateCore;
import org.core.permission.CorePermission;

@SuppressWarnings({"DuplicateStringLiteralInspection", "SpellCheckingInspection"})
public interface Permissions {

    CorePermission CMD_SHIPTYPE_MODIFY_READ = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "cmd", "shiptype", "modify", "get"));
    CorePermission CMD_SHIPTYPE_MODIFY_SET = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "cmd", "shiptype", "modify", "set"));

    CorePermission CMD_LOCK = TranslateCore.getPlatform().register(new CorePermission(false, "ships", "cmd", "lock"));

    CorePermission CMD_SHIPS = TranslateCore.getPlatform().register(new CorePermission(true, "ships", "cmd", "ships"));
    CorePermission AIRSHIP_MOVE_OWN = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "move", "own", "ships", "airship"));
    CorePermission AIRSHIP_MOVE_OTHER = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "move", "other", "ships", "airship"));
    CorePermission AIRSHIP_MAKE = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "make", "ships", "airship"));

    CorePermission WATERSHIP_MOVE_OWN = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "move", "own", "ships", "watership"));
    CorePermission WATERSHIP_MOVE_OTHER = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "move", "other", "ships", "watership"));
    CorePermission WATERSHIP_MAKE = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "make", "ships", "watership"));

    CorePermission MARSSHIP_MOVE_OWN = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "move", "own", "ships", "marsship"));
    CorePermission MARSSHIP_MOVE_OTHER = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "move", "other", "ships", "marsship"));
    CorePermission MARSSHIP_MAKE = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "make", "ships", "marsship"));

    CorePermission PLANE_MOVE_OWN = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "move", "own", "ships", "plane"));
    CorePermission PLANE_MOVE_OTHER = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "move", "other", "ships", "plane"));
    CorePermission PLANE_MAKE = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "make", "ships", "plane"));

    CorePermission SUBMARINE_MOVE_OWN = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "move", "own", "ships", "submarine"));
    CorePermission SUBMARINE_MOVE_OTHER = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "move", "other", "ships", "submarine"));
    CorePermission SUBMARINE_MAKE = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "make", "ships", "submarine"));

    CorePermission OPSHIP_MOVE_OWN = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "move", "own", "ships", "opship"));
    CorePermission OPSHIP_MOVE_OTHER = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "move", "other", "ships", "opship"));
    CorePermission OPSHIP_MAKE = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "make", "ships", "opship"));

    CorePermission SHIP_REMOVE_OTHER = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "remove", "other"));
    CorePermission CMD_INFO = TranslateCore.getPlatform().register(new CorePermission(true, "ships", "cmd", "info"));
    CorePermission CMD_BLOCK_INFO = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "cmd", "blockinfo"));
    CorePermission CMD_SHIPTYPE_CREATE = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "cmd", "shiptype", "create"));
    CorePermission CMD_SHIPTYPE_VIEW_FLAG = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "cmd", "shiptype", "flags"));
    CorePermission CMD_SHIPTYPE_MODIFY_FLAG = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "cmd", "shiptype", "flags", "modify"));

    CorePermission CMD_CONFIG_SET = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "cmd", "config", "set"));
    CorePermission CMD_CONFIG_VIEW = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "cmd", "config", "view"));
    CorePermission CMD_SHIP_AUTOPILOT_USE_OWN = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "cmd", "ship", "autopilot", "use", "own"));
    CorePermission CMD_SHIP_AUTOPILOT_USE_OTHER = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "cmd", "ship", "autopilot", "use", "other"));
    CorePermission CMD_BLOCKLIST_SET = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "cmd", "blocklist", "set"));
    CorePermission CMD_BLOCKLIST_VIEW = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "cmd", "blocklist", "view"));
    CorePermission CMD_SHIP_TRACK_OWN = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "cmd", "ship", "track", "own"));

    CorePermission CMD_SHIP_TRACK_OTHER = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "cmd", "ship", "track", "other"));


    CorePermission CMD_SHIP_MODIFY_SPEED = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "cmd", "ship", "modify", "speed"));
    CorePermission CMD_SHIP_EOT = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "cmd", "ship", "eot"));
    CorePermission CMD_SHIP_CREW = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "cmd", "ship", "crew"));
    CorePermission CMD_SHIP_MOVETO_POSITION = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "cmd", "ship", "moveto", "position"));
    CorePermission CMD_SHIP_MOVETO_ROTATE = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "cmd", "ship", ".moveto", ".rotate"));
    CorePermission CMD_SHIP_TELEPORT = TranslateCore
            .getPlatform()
            .register(new CorePermission(true, "ships", "cmd", "ship", "teleport"));
    CorePermission CMD_SHIP_TELEPORT_SET = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "cmd", "ship", "teleport", "set"));

    CorePermission CMD_SHIP_STRUCTURE_SAVE = TranslateCore
            .getPlatform()
            .register(new CorePermission(false, "ships", "cmd", "ship", "structure", "save"));
}
