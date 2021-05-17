package org.ships.movement;

import org.core.utils.Identifiable;

public interface BlockPriority extends Identifiable {

    BlockPriority ATTACHED = new PackagedBlockPriority("attached", 5);
    BlockPriority DIRECTIONAL = new PackagedBlockPriority("directional", 10);
    BlockPriority NORMAL = new PackagedBlockPriority("normal", 100);
    BlockPriority AIR = new PackagedBlockPriority("air",200);

    int getPriorityNumber();
    BlockPriority setPriorityNumber(int number);
}
