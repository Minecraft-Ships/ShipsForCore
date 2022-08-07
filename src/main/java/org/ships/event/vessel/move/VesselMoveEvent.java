package org.ships.event.vessel.move;

import org.core.event.events.Cancellable;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.event.vessel.VesselEvent;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.Result;
import org.ships.vessel.common.types.Vessel;

import java.util.Set;

public class VesselMoveEvent implements VesselEvent {

    private final Vessel vessel;
    private final MovementContext context;

    public VesselMoveEvent(Vessel vessel, MovementContext context) {
        this.vessel = vessel;
        this.context = context;
    }

    public MovementContext getContext() {
        return this.context;
    }

    @Deprecated
    public BasicMovement getMovement() {
        return this.context.getMovement();
    }

    @Deprecated
    public boolean isStrictedMovement() {
        return this.context.isStrictMovement();
    }

    @Deprecated
    public MovingBlockSet getMovingStructure() {
        return this.context.getMovingStructure();
    }

    @Override
    public Vessel getVessel() {
        return this.vessel;
    }

    public static class Pre extends VesselMoveEvent implements Cancellable {

        private boolean cancelled;

        public Pre(Vessel vessel, MovementContext context) {
            super(vessel, context);
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(boolean value) {
            this.cancelled = value;
        }
    }

    public static class CollideDetected extends VesselMoveEvent {

        private final Set<SyncBlockPosition> collisions;

        public CollideDetected(Vessel vessel, MovementContext context, Set<SyncBlockPosition> collision) {
            super(vessel, context);
            this.collisions = collision;
        }

        public Set<SyncBlockPosition> getCollisions() {
            return this.collisions;
        }
    }

    public static class Main extends VesselMoveEvent implements Cancellable {

        private boolean cancelled;

        public Main(Vessel vessel, MovementContext context) {
            super(vessel, context);
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(boolean value) {
            this.cancelled = value;
        }
    }

    public static class Post extends VesselMoveEvent {

        private final Result result;

        public Post(Vessel vessel, MovementContext context, Result result) {
            super(vessel, context);
            this.result = result;
        }

        public Result getResult() {
            return this.result;
        }

    }
}
