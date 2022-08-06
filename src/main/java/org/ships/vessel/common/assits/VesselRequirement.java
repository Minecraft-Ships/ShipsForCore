package org.ships.vessel.common.assits;

import org.jetbrains.annotations.NotNull;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.vessel.common.requirement.Requirement;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.Optional;

public interface VesselRequirement extends Vessel {

    Collection<Requirement> getRequirements();

    default <R extends Requirement> Optional<R> getRequirement(Class<R> clazz) {
        return this.getRequirements().parallelStream().filter(requirement -> clazz.isInstance(requirement)).findAny().map(requirement -> (R) requirement);
    }

    default void checkRequirements(@NotNull MovementContext context) throws MoveException {
        for (Requirement requirement : this.getRequirements()) {
            requirement.onCheckRequirement(context, this);
        }
    }

    default void finishRequirements(@NotNull MovementContext context) throws MoveException {
        for (Requirement requirement : this.getRequirements()) {
            requirement.onProcessRequirement(context, this);
        }
    }


    @Deprecated(forRemoval = true)
    default void meetsRequirements(MovementContext context) throws MoveException {


    }

    @Deprecated(forRemoval = true)
    default void processRequirements(MovementContext context) throws MoveException {
        int size = this.getStructure().getOriginalRelativePositions().size() + 1;
        if (this.getMaxSize().isPresent() && (this.getMaxSize().get() < size)) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.OVER_SIZED, (size - this.getMaxSize().get())));
        }
        if (this.getMinSize() > size) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.UNDER_SIZED, (this.getMinSize() - size)));
        }
    }

}
