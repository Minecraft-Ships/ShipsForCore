package org.ships.permissions.vessel;

public class AbstractCrewPermission implements CrewPermission{

    protected boolean move;
    protected boolean remove;
    protected boolean command;
    protected String name;

    public AbstractCrewPermission(String name){
        this.name = name;
    }

    @Override
    public boolean canMove() {
        return this.move;
    }

    @Override
    public boolean canCommand() {
        return this.command;
    }

    @Override
    public boolean canRemove() {
        return this.remove;
    }

    @Override
    public CrewPermission setCanMove(boolean check) {
        this.move = check;
        return this;
    }

    @Override
    public CrewPermission setCommand(boolean check) {
        this.command = check;
        return this;
    }

    @Override
    public CrewPermission setRemove(boolean check) {
        this.remove = check;
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
