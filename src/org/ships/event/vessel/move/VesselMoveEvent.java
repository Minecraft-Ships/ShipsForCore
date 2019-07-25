package org.ships.event.vessel.move;

import org.core.entity.LiveEntity;
import org.core.event.events.Cancellable;
import org.core.world.position.BlockPosition;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.event.vessel.VesselEvent;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.Result;
import org.ships.vessel.common.types.Vessel;

import java.util.Map;
import java.util.Set;

public class VesselMoveEvent implements VesselEvent {

    public static class Pre extends VesselMoveEvent implements Cancellable {

        private boolean cancelled;

        public Pre(Vessel vessel, BasicMovement movement, MovingBlockSet movingStructure, Map<LiveEntity, MovingBlock> entities, boolean strict) {
            super(vessel, movement, movingStructure, entities, strict);
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

        private Set<BlockPosition> collisions;

        public CollideDetected(Vessel vessel, BasicMovement movement, MovingBlockSet movingStructure, Map<LiveEntity, MovingBlock> entities, boolean strict, Set<BlockPosition> collision) {
            super(vessel, movement, movingStructure, entities, strict);
            this.collisions = collision;
        }

        public Set<BlockPosition> getCollisions(){
            return this.collisions;
        }
    }

    public static class Main extends VesselMoveEvent implements Cancellable {

        private boolean cancelled;

        public Main(Vessel vessel, BasicMovement movement, MovingBlockSet movingStructure, Map<LiveEntity, MovingBlock> entities, boolean strict) {
            super(vessel, movement, movingStructure, entities, strict);
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

        private Result result;

        public Post(Vessel vessel, BasicMovement movement, MovingBlockSet movingStructure, Map<LiveEntity, MovingBlock> entities, boolean strict, Result result){
            super(vessel, movement, movingStructure, entities, strict);
            this.result = result;
        }

        public Result getResult(){
            return this.result;
        }

    }

    private Vessel vessel;
    private MovingBlockSet movingStructure;
    private Map<LiveEntity, MovingBlock> entities;
    private boolean isStricted;
    private BasicMovement movement;

    public VesselMoveEvent(Vessel vessel, BasicMovement movement, MovingBlockSet movingStructure, Map<LiveEntity, MovingBlock> entities, boolean strict){
        this.vessel = vessel;
        this.movingStructure = movingStructure;
        this.entities = entities;
        this.isStricted = strict;
        this.movement = movement;
    }

    public Map<LiveEntity, MovingBlock> getEntitiesBlocks(){
        return this.entities;
    }

    public Set<LiveEntity> getEntities(){
        return this.entities.keySet();
    }

    public BasicMovement getMovement(){
        return this.movement;
    }

    public boolean isStrictedMovement(){
        return this.isStricted;
    }

    public MovingBlockSet getMovingStructure(){
        return this.movingStructure;
    }

    @Override
    public Vessel getVessel() {
        return this.vessel;
    }
}
