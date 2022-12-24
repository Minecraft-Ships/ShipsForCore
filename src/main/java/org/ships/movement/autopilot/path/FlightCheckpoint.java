package org.ships.movement.autopilot.path;

import org.core.vector.type.Vector3;
import org.core.world.position.Positionable;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.jetbrains.annotations.NotNull;
import org.ships.movement.instruction.details.MovementDetails;
import org.ships.vessel.common.types.Vessel;

public class FlightCheckpoint implements Positionable<BlockPosition> {

    private final @NotNull BlockPosition position;
    private final int allowedDistance;

    public FlightCheckpoint(@NotNull BlockPosition position) {
        this(position, 3);
    }

    public FlightCheckpoint(@NotNull BlockPosition position, int allowedDistance) {
        this.position = position;
        this.allowedDistance = allowedDistance;
    }

    public void moveTowards(@NotNull Vessel vessel, @NotNull MovementDetails details) {
        if (!vessel.getPosition().getWorld().equals(this.position.getWorld())) {
            throw new IllegalArgumentException(
                    "Vessel cannot move towards this flight checkpoint as the worlds do not match");
        }
        vessel.moveTowards(this.position.getPosition(), details);
    }

    public int getAllowedDistance() {
        return this.allowedDistance;
    }

    @Override
    public @NotNull BlockPosition getPosition() {
        return this.position;
    }

    public boolean hasReached(@NotNull Positionable<?> positionable) {
        return this.hasReached(positionable.getPosition());
    }

    public boolean hasReached(@NotNull Position<?> position) {
        if (!this.position.getWorld().equals(position.getWorld())) {
            throw new IllegalArgumentException("Position is not in world");
        }
        return this.hasReached(position.getPosition());
    }

    public boolean hasReached(@NotNull Vector3<?> position) {
        return this.position.getPosition().distanceSquared(position) <= this.allowedDistance;
    }
}
