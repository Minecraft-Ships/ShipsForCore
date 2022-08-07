package org.ships.vessel.common.requirement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;
import java.util.OptionalInt;

public class MaxSizeRequirement implements Requirement {

    private final @Nullable Integer maxSize;
    private final @Nullable MaxSizeRequirement parent;

    public MaxSizeRequirement(@NotNull MaxSizeRequirement parent) {
        this(parent, null);
    }

    public MaxSizeRequirement(@Nullable MaxSizeRequirement parent, @Nullable Integer maxSize) {
        if (maxSize != null && maxSize < 0) {
            throw new IllegalArgumentException("Max size cannot be less then 0");
        }
        this.maxSize = maxSize;
        this.parent = parent;
    }

    public boolean isMaxSizeSpecified() {
        return this.maxSize != null;
    }

    public OptionalInt getMaxSize() {
        if (this.maxSize == null) {
            if (this.parent == null) {
                return OptionalInt.empty();
            }
            return this.parent.getMaxSize();
        }
        return OptionalInt.of(this.maxSize);
    }

    @Override
    public boolean useOnStrict() {
        return false;
    }

    @Override
    public void onCheckRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {
        int size = context.getMovingStructure().size() + 1;
        OptionalInt opMaxSize = this.getMaxSize();
        if (opMaxSize.isEmpty()) {
            return;
        }
        if (opMaxSize.getAsInt() < size) {
            throw new MoveException(
                    new AbstractFailedMovement<>(vessel, MovementResult.OVER_SIZED, (size - opMaxSize.getAsInt())));
        }
    }

    @Override
    public void onProcessRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {

    }

    @Override
    public @NotNull Requirement createChild() {
        return new MaxSizeRequirement(this);
    }

    public Requirement createChild(@Nullable Integer value) {
        return new MaxSizeRequirement(this, value);
    }

    @Override
    public Optional<Requirement> getParent() {
        return Optional.ofNullable(this.parent);
    }
}
