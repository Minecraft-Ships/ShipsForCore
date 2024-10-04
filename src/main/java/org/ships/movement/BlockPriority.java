package org.ships.movement;

import org.core.utils.Identifiable;

public interface BlockPriority extends Identifiable {

    @Deprecated(forRemoval = true)
    BlockPriority ATTACHED = BlockPriorities.ATTACHED;

    @Deprecated(forRemoval = true)
    BlockPriority DIRECTIONAL = BlockPriorities.DIRECTIONAL;

    @Deprecated(forRemoval = true)
    BlockPriority NORMAL = BlockPriorities.NORMAL;

    @Deprecated(forRemoval = true)
    BlockPriority AIR = BlockPriorities.AIR;

    int getPriorityNumber();

    BlockPriority setPriorityNumber(int number);
}
