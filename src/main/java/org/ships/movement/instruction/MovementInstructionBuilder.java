package org.ships.movement.instruction;

import org.core.vector.type.Vector3;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.movement.Movement;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.SetMovingBlock;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MovementInstructionBuilder {

    private MovingBlockSet movingBlocks;
    private boolean strictMovement;
    private Movement.MidMovement[] midMoveEvent;
    private Movement.PostMovement[] postMoveEvent;
    private Consumer<? super Throwable> exception;
    private BasicMovement movementAlgorithm;

    public MovingBlockSet getMovingBlocks() {
        return this.movingBlocks;
    }

    public MovementInstructionBuilder setMovingBlocks(MovingBlockSet movingBlocks) {
        this.movingBlocks = movingBlocks;
        return this;
    }

    public MovementInstructionBuilder setAddToMovementBlocks(PositionableShipsStructure structure, int x, int y,
            int z) {
        return this.setAddToMovementBlocks(structure, Vector3.valueOf(x, y, z));
    }

    public MovementInstructionBuilder setAddToMovementBlocks(PositionableShipsStructure structure,
            Vector3<Integer> addTo) {
        return this.setMovementBlocks(structure, position -> new SetMovingBlock(position, position.getRelative(addTo)));
    }

    public MovementInstructionBuilder setMovementBlocks(PositionableShipsStructure structure,
            Function<SyncBlockPosition, MovingBlock> function) {
        this.movingBlocks = structure
                .getSyncedPositions()
                .stream()
                .map(function)
                .collect(Collectors.toCollection(MovingBlockSet::new));
        return this;
    }

    public boolean isStrictMovement() {
        return this.strictMovement;
    }

    public MovementInstructionBuilder setStrictMovement(boolean strictMovement) {
        this.strictMovement = strictMovement;
        return this;
    }

    public @NotNull Movement.MidMovement[] getMidMoveEvent() {
        if(this.midMoveEvent == null){
            return new Movement.MidMovement[0];
        }
        return this.midMoveEvent;
    }

    public MovementInstructionBuilder setMidMoveEvent(Movement.MidMovement... midMoveEvent) {
        this.midMoveEvent = midMoveEvent;
        return this;
    }

    public @NotNull Movement.PostMovement[] getPostMoveEvent() {
        if(this.postMoveEvent == null){
            return new Movement.PostMovement[0];
        }
        return this.postMoveEvent;
    }

    public MovementInstructionBuilder setPostMoveEvent(Movement.PostMovement... postMoveEvent) {
        this.postMoveEvent = postMoveEvent;
        return this;
    }

    public Consumer<? super Throwable> getException() {
        return this.exception;
    }

    public MovementInstructionBuilder setException(Consumer<? super Throwable> exception) {
        this.exception = exception;
        return this;
    }

    public BasicMovement getMovementAlgorithm() {
        return this.movementAlgorithm;
    }

    public MovementInstructionBuilder setMovementAlgorithm(BasicMovement movementAlgorithm) {
        this.movementAlgorithm = movementAlgorithm;
        return this;
    }


    public MovementInstruction build() {
        return new MovementInstruction(this);
    }
}
