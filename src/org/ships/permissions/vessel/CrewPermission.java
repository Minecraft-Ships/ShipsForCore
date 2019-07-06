package org.ships.permissions.vessel;

import org.core.utils.Identifable;

public interface CrewPermission extends Identifable {

    CrewPermission CAPTAIN = new AbstractCrewPermission("ships.captain", "Captain").setCanMove(true).setCommand(true).setRemove(true);
    CrewPermission DEFAULT = new AbstractCrewPermission("ships.default", "Default").setCanMove(false).setCommand(false).setRemove(false);

    boolean canMove();
    boolean canCommand();
    boolean canRemove();

    CrewPermission setCanMove(boolean check);
    CrewPermission setCommand(boolean check);
    CrewPermission setRemove(boolean check);

}
