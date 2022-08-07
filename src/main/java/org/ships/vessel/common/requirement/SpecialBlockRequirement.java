package org.ships.vessel.common.requirement;

import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;

public class SpecialBlockRequirement implements Requirement {

    private final @Nullable BlockType blockType;
    private final @Nullable Integer amount;
    private final @Nullable SpecialBlockRequirement parent;
    private final @Nullable String displayName;

    public SpecialBlockRequirement(@NotNull SpecialBlockRequirement parent, @Nullable String name) {
        this(parent, null, null, name);
    }

    public SpecialBlockRequirement(@Nullable SpecialBlockRequirement parent, @Nullable BlockType type,
            @Nullable Integer amount, @Nullable String name) {
        if (parent == null && (type == null || amount == null)) {
            throw new IllegalArgumentException("parent cannot be null if another value is");
        }
        this.amount = amount;
        this.blockType = type;
        this.parent = parent;
        this.displayName = name;
    }

    public Optional<String> getDisplayName() {
        if (this.parent == null) {
            return Optional.ofNullable(this.displayName);
        }
        return this.parent.getDisplayName();
    }

    public int getAmount() {
        if (this.parent == null) {
            if (this.amount == null) {
                throw new RuntimeException("You skipped the constructor checks");
            }
            return this.amount;
        }
        return this.parent.getAmount();
    }

    public @NotNull BlockType getBlock() {
        if (this.parent == null) {
            if (this.blockType == null) {
                throw new RuntimeException("You skipped the constructor checks");
            }
            return this.blockType;
        }
        return this.parent.getBlock();
    }

    @Override
    public boolean useOnStrict() {
        return false;
    }

    @Override
    public void onCheckRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {
        BlockType requiredType = this.getBlock();
        int amount = this.getAmount();

        long found = context
                .getMovingStructure()
                .parallelStream()
                .map(moving -> moving.getStoredBlockData().getType().equals(requiredType))
                .count();
        if (found > amount) {
            throw new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.NO_SPECIAL_NAMED_BLOCK_FOUND,
                    this.getDisplayName().orElse(requiredType.getName())));
        }
    }

    @Override
    public void onProcessRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {

    }

    @Override
    public @NotNull Requirement createChild() {
        return new SpecialBlockRequirement(this, this.displayName);
    }

    @Override
    public Optional<Requirement> getParent() {
        return Optional.ofNullable(this.parent);
    }
}