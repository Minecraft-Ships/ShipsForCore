package org.ships.movement.instruction;

import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.instruction.actions.MidMovement;
import org.ships.movement.instruction.actions.PostMovement;

import java.util.function.Consumer;

public class MovementInstruction {

    private final @NotNull MovingBlockSet movingBlocks;
    private final boolean strictMovement;
    private final MidMovement[] midMoveEvent;
    private final PostMovement[] postMoveEvent;
    private final @NotNull BasicMovement movementAlgorithm;

    public MovementInstruction(MovementInstructionBuilder builder) {
        this.movementAlgorithm = builder.getMovementAlgorithm();
        this.midMoveEvent = builder.getMidMoveEvent();
        this.postMoveEvent = builder.getPostMoveEvent();
        this.strictMovement = builder.isStrictMovement();
        this.movingBlocks = builder.getMovingBlocks();
        if (this.movingBlocks.isEmpty()) {
            throw new IllegalStateException("Moving blocks are required");
        }
        if (this.movementAlgorithm == null) {
            throw new IllegalStateException("Movement algorithm is required");
        }
    }

    public @NotNull MovingBlockSet getMovingBlocks() {
        return this.movingBlocks;
    }

    public boolean isStrictMovement() {
        return this.strictMovement;
    }

    public MidMovement[] getMidMoveEvent() {
        return this.midMoveEvent;
    }

    public PostMovement[] getPostMoveEvent() {
        return this.postMoveEvent;
    }

    public @NotNull BasicMovement getMovementAlgorithm() {
        return this.movementAlgorithm;
    }
}
