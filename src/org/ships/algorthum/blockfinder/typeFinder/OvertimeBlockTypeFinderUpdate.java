package org.ships.algorthum.blockfinder.typeFinder;

import org.core.world.position.BlockPosition;

public interface OvertimeBlockTypeFinderUpdate {

    void onBlockFound(BlockPosition position);
    void onFailedToFind();
}
