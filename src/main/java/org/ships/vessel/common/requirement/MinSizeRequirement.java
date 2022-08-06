package org.ships.vessel.common.requirement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;

public class MinSizeRequirement implements Requirement {

    private final @Nullable MinSizeRequirement parent;
    private final @Nullable Integer minSize;

    public MinSizeRequirement(@Nullable MinSizeRequirement parent, @Nullable Integer minSize) {
        if (parent == null && minSize == null) {
            throw new IllegalArgumentException("Both parent and Min size cannot be null");
        }
        this.minSize = minSize;
        this.parent = parent;
    }

    public int getMinimumSize() {
        if (this.minSize != null) {
            return this.minSize;
        }
        if (this.parent == null) {
            throw new RuntimeException("You managed to get passed the constructor check. What you are doing is not supported");
        }
        return this.parent.getMinimumSize();
    }

    @Override
    public boolean useOnStrict() {
        return false;
    }

    @Override
    public void onCheckRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {
        int shipSize = context.getMovingStructure().size() + 1;
        int minSize = this.getMinimumSize();

        if (minSize > shipSize) {
            throw new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.UNDER_SIZED, (minSize - shipSize)));
        }
    }

    @Override
    public void onProcessRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {

    }

    @Override
    public @NotNull Requirement createChild() {
        return null;
    }

    @Override
    public Optional<Requirement> getParent() {
        return Optional.empty();
    }
}
