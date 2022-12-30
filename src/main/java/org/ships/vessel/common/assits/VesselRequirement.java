package org.ships.vessel.common.assits;

import org.jetbrains.annotations.NotNull;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.vessel.common.requirement.Requirement;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.Optional;

public interface VesselRequirement extends Vessel {

    Collection<Requirement<?>> getRequirements();

    default <R extends Requirement<?>> Optional<R> getRequirement(Class<R> clazz) {
        Collection<Requirement<?>> requirements = this.getRequirements();
        return requirements
                .parallelStream()
                .filter(clazz::isInstance)
                .findAny()
                .map(requirement -> (R) requirement);
    }

    default void checkRequirements(@NotNull MovementContext context) throws MoveException {
        for (Requirement<?> requirement : this.getRequirements()) {
            requirement.onCheckRequirement(context, this);
        }
    }

    default void finishRequirements(@NotNull MovementContext context) throws MoveException {
        for (Requirement<?> requirement : this.getRequirements()) {
            requirement.onProcessRequirement(context, this);
        }
    }

    void setRequirement(@NotNull Requirement<?> updated);
}
