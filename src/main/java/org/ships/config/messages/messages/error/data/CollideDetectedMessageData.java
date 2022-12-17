package org.ships.config.messages.messages.error.data;

import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.Collections;

public class CollideDetectedMessageData {

    private final @NotNull Vessel vessel;
    private final @NotNull Collection<BlockPosition> positions;

    public CollideDetectedMessageData(@NotNull Vessel vessel, Collection<BlockPosition> positions) {
        this.vessel = vessel;
        this.positions = Collections.unmodifiableCollection(positions);
    }

    public Vessel getVessel() {
        return this.vessel;
    }

    public Collection<BlockPosition> getPositions() {
        return this.positions;
    }
}
