package org.ships.plugin;

import org.ships.movement.BlockPriority;

import java.util.Comparator;

public class ShipsPlugin {

    public static final Comparator<BlockPriority> SORT_BLOCK_PRIORITY = ((bp1, bp2) -> {
        if(bp1.getPriorityNumber() < bp2.getPriorityNumber()){
            return -1;
        }else if(bp1.getPriorityNumber() > bp2.getPriorityNumber()){
            return 1;
        }
        return 0;
    });
}
