package org.ships.vessel.common.requirement;

import org.jetbrains.annotations.NotNull;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;

public interface Requirement<S extends Requirement<S>> {

    boolean useOnStrict();

    void onCheckRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException;

    void onProcessRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException;

    @NotNull S getRequirementsBetween(@NotNull S requirement);

    @NotNull S createChild();

    @NotNull S createCopy();

    Optional<S> getParent();

    boolean isEnabled();
}
