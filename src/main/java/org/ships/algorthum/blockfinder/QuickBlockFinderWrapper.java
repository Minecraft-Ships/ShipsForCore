package org.ships.algorthum.blockfinder;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.config.node.DedicatedNode;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class QuickBlockFinderWrapper implements BasicBlockFinder {

    private final @NotNull BasicBlockFinder finder;

    public QuickBlockFinderWrapper(@NotNull BasicBlockFinder finder) {
        this.finder = finder;
    }

    @Override
    public @NotNull BasicBlockFinder init() {
        return this.finder.init();
    }

    @Override
    public CompletableFuture<PositionableShipsStructure> getConnectedBlocksOvertime(@NotNull BlockPosition position,
                                                                                    @NotNull OvertimeBlockFinderUpdate runAfterFullSearch) {
        return this.finder.getConnectedBlocksOvertime(position, runAfterFullSearch);
    }

    @Override
    public int getBlockLimit() {
        return this.finder.getBlockLimit();
    }

    @Override
    public @NotNull BasicBlockFinder setBlockLimit(int limit) {
        return this.finder.setBlockLimit(limit);
    }

    @Override
    public Optional<Vessel> getConnectedVessel() {
        return this.finder.getConnectedVessel();
    }

    @Override
    public @NotNull BasicBlockFinder setConnectedVessel(@Nullable Vessel vessel) {
        return this.finder.setConnectedVessel(vessel);
    }

    @Override
    public String getId() {
        return this.finder.getId();
    }

    @Override
    public String getName() {
        return this.finder.getName();
    }

}
