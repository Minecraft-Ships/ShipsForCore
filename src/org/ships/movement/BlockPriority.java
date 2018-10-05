package org.ships.movement;

import org.core.utils.Identifable;

public abstract class BlockPriority implements Identifable {

    public static final BlockPriority ATTACHED = new AbstractBlockPriority("attached", 1);

    private static class AbstractBlockPriority extends BlockPriority {

        private int number;
        private String name;

        public AbstractBlockPriority(String name, int priorty){
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

    public abstract int getPriorityNumber();
}
