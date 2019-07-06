package org.ships.permissions.vessel;

public class AbstractCrewPermission implements CrewPermission{

    protected boolean move;
    protected boolean remove;
    protected boolean command;
    protected String name;
    protected String id;

    public AbstractCrewPermission(String id, String name){
        this.name = name;
        this.id = id;
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
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof CrewPermission)){
            return false;
        }
        CrewPermission permission = (CrewPermission)obj;
        return permission.getId().equals(this.getId());
    }
}
