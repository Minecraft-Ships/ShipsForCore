package org.ships.movement.instruction;

import org.core.vector.type.Vector3;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.SetMovingBlock;
import org.ships.movement.instruction.actions.MidMovement;
import org.ships.movement.instruction.actions.PostMovement;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MovementInstructionBuilder {

    private MovingBlockSet movingBlocks;
    private boolean strictMovement;
    private MidMovement[] midMoveEvent;
    private PostMovement[] postMoveEvent;
    private BasicMovement movementAlgorithm;


    public MovingBlockSet getMovingBlocks() {
        return this.movingBlocks;
    }

    public MovementInstructionBuilder setMovingBlocks(MovingBlockSet movingBlocks) {
        this.movingBlocks = movingBlocks;
        return this;
    }

    public MovementInstructionBuilder setTeleportToMovementBlocks(PositionableShipsStructure structure,
                                                                  BlockPosition position) {
        SyncBlockPosition syncedBlock = position.toSyncPosition();
        return this.setMovementBlocks(structure, block -> {
            Vector3<Integer> relative = block.getPosition().minus(structure.getPosition().getPosition());
            SyncBlockPosition newType = syncedBlock.getRelative(relative);
            return new SetMovingBlock(block, newType);
        });
    }

    public MovementInstructionBuilder setAddToMovementBlocks(PositionableShipsStructure structure,
                                                             int x,
                                                             int y,
                                                             int z) {
        return this.setAddToMovementBlocks(structure, Vector3.valueOf(x, y, z));
    }

    public MovementInstructionBuilder setAddToMovementBlocks(PositionableShipsStructure structure,
                                                             Vector3<Integer> addTo) {
        return this.setMovementBlocks(structure, position -> new SetMovingBlock(position, position.getRelative(addTo)));
    }

    public MovementInstructionBuilder setRotateLeftAroundPosition(PositionableShipsStructure structure,
                                                                  BlockPosition position) {
        return this.setMovementBlocks(structure, block -> new SetMovingBlock(block, block).rotateLeft(position));
    }

    public MovementInstructionBuilder setRotateLeftAroundPosition(PositionableShipsStructure structure) {
        return this.setRotateLeftAroundPosition(structure, structure.getPosition());
    }

    public MovementInstructionBuilder setRotateRightAroundPosition(PositionableShipsStructure structure,
                                                                   BlockPosition position) {
        return this.setMovementBlocks(structure, block -> new SetMovingBlock(block, block).rotateRight(position));
    }

    public MovementInstructionBuilder setRotateRightAroundPosition(PositionableShipsStructure structure) {
        return this.setRotateRightAroundPosition(structure, structure.getPosition());
    }

    public MovementInstructionBuilder setMovementBlocks(PositionableShipsStructure structure,
                                                        Function<SyncBlockPosition, MovingBlock> function) {
        this.movingBlocks = structure
                .getPositionsRelativeToWorld()
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

    public @NotNull MidMovement[] getMidMoveEvent() {
        if (this.midMoveEvent == null) {
            return new MidMovement[0];
        }
        return this.midMoveEvent;
    }

    public MovementInstructionBuilder setMidMoveEvent(MidMovement... midMoveEvent) {
        this.midMoveEvent = midMoveEvent;
        return this;
    }

    public @NotNull PostMovement[] getPostMoveEvent() {
        if (this.postMoveEvent == null) {
            return new PostMovement[0];
        }
        return this.postMoveEvent;
    }

    public MovementInstructionBuilder setPostMoveEvent(PostMovement... postMoveEvent) {
        this.postMoveEvent = postMoveEvent;
        return this;
    }

    public @NotNull BasicMovement getMovementAlgorithm() {
        if (this.movementAlgorithm == null) {
            return ShipsPlugin.getPlugin().getConfig().getDefaultMovement();
        }
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
