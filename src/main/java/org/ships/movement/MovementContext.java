package org.ships.movement;

import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.impl.BlockPosition;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.event.vessel.move.VesselMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class MovementContext {

    protected ServerBossBar bar;
    protected BlockPosition clicked;
    protected boolean strictMovement;
    protected MovingBlockSet blocks;
    protected BasicMovement movement;
    protected Map<EntitySnapshot<? extends LiveEntity>, MovingBlock> entities = new HashMap<>();
    protected Movement.MidMovement[] midMovementProcess = new Movement.MidMovement[0];
    protected Movement.PostMovement[] postMovementProcess = new Movement.PostMovement[0];
    protected Consumer<VesselMoveEvent.Post> post = (e) -> {};

    public Optional<BlockPosition> getClicked(){
        return Optional.ofNullable(this.clicked);
    }

    public MovementContext setClicked(BlockPosition position){
        this.clicked = position;
        return this;
    }

    public Optional<ServerBossBar> getBar(){
        return Optional.ofNullable(this.bar);
    }

    public MovementContext setBar(ServerBossBar bar){
        this.bar = bar;
        return this;
    }

    public boolean isStrictMovement(){
        return this.strictMovement;
    }

    public MovementContext setStrictMovement(boolean check){
        this.strictMovement = check;
        return this;
    }

    public MovingBlockSet getMovingStructure(){
        return this.blocks;
    }

    public MovementContext setMovingStructure(MovingBlockSet set){
        this.blocks = set;
        return this;
    }

    public BasicMovement getMovement(){
        return this.movement;
    }

    public MovementContext setMovement(BasicMovement movement){
        this.movement = movement;
        return this;
    }

    public Map<EntitySnapshot<? extends LiveEntity>, MovingBlock> getEntities(){
        return this.entities;
    }

    public Movement.PostMovement[] getPostMovementProcess(){
        return this.postMovementProcess;
    }

    public MovementContext setPostMovementProcess(Movement.PostMovement... postMovement){
        this.postMovementProcess = postMovement;
        return this;
    }

    public Movement.MidMovement[] getMidMovementProcess(){
        return this.midMovementProcess;
    }

    public MovementContext setMidMovementProcess(Movement.MidMovement... midMovement){
        this.midMovementProcess = midMovement;
        return this;
    }

    public Consumer<VesselMoveEvent.Post> getPostMovement(){
        return this.post;
    }

    public MovementContext setPostMovement(Consumer<VesselMoveEvent.Post> consumer){
        this.post = consumer;
        return this;
    }
}
