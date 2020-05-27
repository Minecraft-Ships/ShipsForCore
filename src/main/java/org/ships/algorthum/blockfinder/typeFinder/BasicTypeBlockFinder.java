package org.ships.algorthum.blockfinder.typeFinder;

import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.algorthum.Algorthum;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;
import java.util.function.Predicate;

public interface BasicTypeBlockFinder extends Algorthum {

    Ships5BlockTypeFinder SHIPS_FIVE = new Ships5BlockTypeFinder();
    Ships6BlockTypeFinder SHIPS_SIX = new Ships6BlockTypeFinder();

    BasicTypeBlockFinder init();
    Optional<SyncBlockPosition> findBlock(SyncBlockPosition position, Predicate<SyncBlockPosition> predicate);
    void findBlock(SyncBlockPosition position, Predicate<SyncBlockPosition> predicate, OvertimeBlockTypeFinderUpdate runAfterSearch);
    int getBlockLimit();
    BasicTypeBlockFinder setBlockLimit(int limit);
    Optional<Vessel> getConnectedVessel();
    BasicTypeBlockFinder setConnectedVessel(Vessel vessel);

}
