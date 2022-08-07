package org.ships.movement.instruction;

import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.movement.Movement;
import org.ships.movement.MovingBlockSet;

import java.util.function.Consumer;

public class MovementInstruction {

    private final @NotNull MovingBlockSet movingBlocks;
    private final boolean strictMovement;
    private final @NotNull Movement.MidMovement[] midMoveEvent;
    private final @NotNull Movement.PostMovement[] postMoveEvent;
    private final @NotNull Consumer<? super Throwable> exception;
    private final @NotNull BasicMovement movementAlgorithm;

    public MovementInstruction(MovementInstructionBuilder builder){
        this.movementAlgorithm = builder.getMovementAlgorithm();
        this.midMoveEvent = builder.getMidMoveEvent();
        this.postMoveEvent = builder.getPostMoveEvent();
        this.strictMovement = builder.isStrictMovement();
        this.exception = builder.getException();
        this.movingBlocks = builder.getMovingBlocks();
    }

    public @NotNull MovingBlockSet getMovingBlocks() {
        return this.movingBlocks;
    }

    public boolean isStrictMovement() {
        return this.strictMovement;
    }

    public Movement.MidMovement[] getMidMoveEvent() {
        return this.midMoveEvent;
    }

    public Movement.PostMovement[] getPostMoveEvent() {
        return this.postMoveEvent;
    }

    public @NotNull Consumer<? super Throwable> getException() {
        return this.exception;
    }

    public @NotNull BasicMovement getMovementAlgorithm() {
        return this.movementAlgorithm;
    }
}
