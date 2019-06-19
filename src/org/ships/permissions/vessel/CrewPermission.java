package org.ships.permissions.vessel;

public interface CrewPermission {

    CrewPermission CAPTAIN = new AbstractCrewPermission("Captain").setCanMove(true).setCommand(true).setRemove(true);

    boolean canMove();
    boolean canCommand();
    boolean canRemove();

    CrewPermission setCanMove(boolean check);
    CrewPermission setCommand(boolean check);
    CrewPermission setRemove(boolean check);

    String getName();
}
