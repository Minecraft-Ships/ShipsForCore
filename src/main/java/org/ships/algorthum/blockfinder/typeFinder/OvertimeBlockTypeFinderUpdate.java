package org.ships.algorthum.blockfinder.typeFinder;

import org.core.world.position.impl.sync.SyncBlockPosition;

public interface OvertimeBlockTypeFinderUpdate {

    void onBlockFound(SyncBlockPosition position);
    void onFailedToFind();
}
