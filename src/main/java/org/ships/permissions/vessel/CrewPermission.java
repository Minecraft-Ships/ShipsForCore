package org.ships.permissions.vessel;

import org.core.utils.Identifiable;

public interface CrewPermission extends Identifiable {

    CrewPermission CAPTAIN = CrewPermissions.CAPTAIN;
    CrewPermission CREW_MEMBER = CrewPermissions.CREW_MEMBER;
    CrewPermission DEFAULT = CrewPermissions.DEFAULT;

    boolean canMove();

    boolean canCommand();

    boolean canRemove();

    CrewPermission setCanMove(boolean check);

    CrewPermission setCommand(boolean check);

    CrewPermission setRemove(boolean check);

}
