package org.ships.vessel.common.requirement;

import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlock;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.movement.result.data.RequiredPercentMovementData;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.Optional;

public class SpecialBlocksRequirement implements Requirement {

    private final @Nullable SpecialBlocksRequirement parent;
    private final @Nullable Collection<BlockType> specialBlocks;
    private final @Nullable Float specialBlocksPercentage;

    public SpecialBlocksRequirement(@NotNull SpecialBlocksRequirement parent) {
        this(parent, null, null);
    }

    public SpecialBlocksRequirement(@Nullable SpecialBlocksRequirement parent, @Nullable Float specialBlocksPercentage,
            @Nullable Collection<BlockType> specialBlocks) {
        if (parent == null && (specialBlocks == null || specialBlocksPercentage == null)) {
            throw new IllegalArgumentException("Parent cannot be null if another value is null");
        }
        if (specialBlocksPercentage != null && (specialBlocksPercentage < 0 || specialBlocksPercentage > 100)) {
            throw new IndexOutOfBoundsException("Percentage can only be between 0 and 100");
        }
        this.parent = parent;
        this.specialBlocks = specialBlocks;
        this.specialBlocksPercentage = specialBlocksPercentage;
    }

    public boolean isPercentageSpecified() {
        return this.specialBlocksPercentage != null;
    }

    public boolean isBlocksSpecified() {
        return this.specialBlocks != null;
    }

    public float getPercentage() {
        if (this.parent == null) {
            if (this.specialBlocksPercentage == null) {
                throw new RuntimeException("You skipped the constructor checks");
            }
            return this.specialBlocksPercentage;
        }
        return this.parent.getPercentage();
    }

    public @NotNull Collection<BlockType> getBlocks() {
        if (this.parent == null) {
            if (this.specialBlocks == null) {
                throw new RuntimeException("You skipped the constructor checks");
            }
            return this.specialBlocks;
        }
        return this.parent.getBlocks();
    }


    @Override
    public boolean useOnStrict() {
        return false;
    }

    @Override
    public void onCheckRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {
        Collection<BlockType> specialBlocks = this.getBlocks();
        float percentageRequired = this.getPercentage();

        long blocksFound = context
                .getMovingStructure()
                .parallelStream()
                .map(MovingBlock::getStoredBlockData)
                .filter(block -> specialBlocks.contains(block.getType()))
                .count();
        if (blocksFound == 0) {
            throw new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.NOT_ENOUGH_PERCENT,
                    new RequiredPercentMovementData(specialBlocks.iterator().next(), percentageRequired,
                            0)));
        }

        int totalPercent = context.getMovingStructure().size() / (int) blocksFound;
        if (totalPercent < percentageRequired) {
            throw new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.NOT_ENOUGH_PERCENT,
                    new RequiredPercentMovementData(specialBlocks.iterator().next(), percentageRequired,
                            totalPercent)));
        }
    }

    @Override
    public void onProcessRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {

    }

    @Override
    public @NotNull SpecialBlocksRequirement createChild() {
        return new SpecialBlocksRequirement(this);
    }

    public @NotNull SpecialBlocksRequirement createChildWithPercentage(@Nullable Float value) {
        return new SpecialBlocksRequirement(this, value, this.specialBlocks);
    }

    public @NotNull SpecialBlocksRequirement createChildWithBlocks(@Nullable Collection<BlockType> blocks) {
        return new SpecialBlocksRequirement(this, this.specialBlocksPercentage, blocks);
    }

    @Override
    public @NotNull SpecialBlocksRequirement createCopy() {
        return new SpecialBlocksRequirement(this.parent, this.specialBlocksPercentage, this.specialBlocks);
    }

    public @NotNull SpecialBlocksRequirement createCopyWithPercentage(@Nullable Float value) {
        return new SpecialBlocksRequirement(this.parent, value, this.specialBlocks);
    }

    public @NotNull SpecialBlocksRequirement createCopyWithBlocks(@Nullable Collection<BlockType> blocks) {
        return new SpecialBlocksRequirement(this.parent, this.specialBlocksPercentage, blocks);
    }


    @Override
    public Optional<Requirement> getParent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public boolean isEnabled() {
        if (this.getBlocks().isEmpty()) {
            return false;
        }
        return this.getPercentage() != 0;
    }
}
