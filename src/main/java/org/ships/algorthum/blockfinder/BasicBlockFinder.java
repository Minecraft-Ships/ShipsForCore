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

    Ships5BlockFinder SHIPS_FIVE = new Ships5BlockFinder();
    Ships5AsyncBlockFinder SHIPS_FIVE_ASYNC = new Ships5AsyncBlockFinder();
    Ships6BlockFinder SHIPS_SIX = new Ships6BlockFinder();
    Ships6MultiAsyncBlockFinder SHIPS_SIX_RELEASE_ONE_MULTI_ASYNC = new Ships6MultiAsyncBlockFinder();
    Ships6SingleAsyncBlockFinder SHIPS_SIX_RELEASE_ONE_SINGLE_ASYNC = new Ships6SingleAsyncBlockFinder();

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
