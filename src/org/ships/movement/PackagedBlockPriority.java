package org.ships.movement;

class PackagedBlockPriority implements BlockPriority{

    private int number;
    private String name;

    public PackagedBlockPriority(String name, int priorty){
        this.number = priorty;
        this.name = name;
    }

    @Override
    public int getPriorityNumber() {
        return this.number;
    }

    @Override
    public String getId() {
        return "ships:" + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return this.name;
    }
}
