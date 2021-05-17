package org.ships.permissions.vessel;

import org.core.utils.Identifiable;

public interface CrewPermission extends Identifiable {

    CrewPermission CAPTAIN = new AbstractCrewPermission("ships.captain", "Captain").setCanMove(true).setCommand(true).setRemove(true);
    CrewPermission CREW_MEMBER = new AbstractCrewPermission("ships.member", "Crew").setCanMove(true).setCommand(false).setRemove(false);
    CrewPermission DEFAULT = new AbstractCrewPermission("ships.default", "Default").setCanMove(false).setCommand(false).setRemove(false);

    boolean canMove();
    boolean canCommand();
    boolean canRemove();

    CrewPermission setCanMove(boolean check);
    CrewPermission setCommand(boolean check);
    CrewPermission setRemove(boolean check);

}
