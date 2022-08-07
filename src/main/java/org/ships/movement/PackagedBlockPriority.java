package org.ships.movement;

class PackagedBlockPriority implements BlockPriority {

    private final String name;
    private int number;

    PackagedBlockPriority(String name, int priority) {
        this.number = priority;
        this.name = name;
    }

    @Override
    public int getPriorityNumber() {
        return this.number;
    }

    @Override
    public BlockPriority setPriorityNumber(int number) {
        this.number = number;
        return this;
    }

    @Override
    public String getId() {
        return "ships:" + this.getName().toLowerCase();
    }

    @Override
    public String getName() {
        return this.name;
    }
}
