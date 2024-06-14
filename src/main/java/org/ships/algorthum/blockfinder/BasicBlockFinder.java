package org.ships.algorthum.blockfinder;

import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.algorthum.Algorithm;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface BasicBlockFinder extends Algorithm {

    @Deprecated(forRemoval = true)
    Ships5BlockFinder SHIPS_FIVE = BlockFinders.SHIPS_FIVE_SYNCED;

    @Deprecated(forRemoval = true)
    Ships5AsyncBlockFinder SHIPS_FIVE_ASYNC = BlockFinders.SHIPS_FIVE_ASYNCED;

    @Deprecated(forRemoval = true)
    Ships6BlockFinder SHIPS_SIX = BlockFinders.SHIPS_SIX_RELEASE_TWO_SYNCED;

    @Deprecated(forRemoval = true)
    Ships6MultiAsyncBlockFinder SHIPS_SIX_RELEASE_ONE_MULTI_ASYNC = BlockFinders.SHIPS_SIX_RELEASE_ONE_ASYNC_MULTI_THREADED;

    @Deprecated(forRemoval = true)
    Ships6SingleAsyncBlockFinder SHIPS_SIX_RELEASE_ONE_SINGLE_ASYNC = BlockFinders.SHIPS_SIX_RELEASE_ONE_ASYNC_SINGLE_THREADED;

    @NotNull BasicBlockFinder init();

    CompletableFuture<PositionableShipsStructure> getConnectedBlocksOvertime(@NotNull BlockPosition position,
                                                                             @NotNull OvertimeBlockFinderUpdate runAfterFullSearch);

    default CompletableFuture<PositionableShipsStructure> getConnectedBlocksOvertime(@NotNull BlockPosition position) {
        return getConnectedBlocksOvertime(position,
                                          (currentStructure, block) -> OvertimeBlockFinderUpdate.BlockFindControl.USE);
    }

    int getBlockLimit();

    @NotNull BasicBlockFinder setBlockLimit(int limit);

    Optional<Vessel> getConnectedVessel();

    @NotNull BasicBlockFinder setConnectedVessel(@Nullable Vessel vessel);

}
