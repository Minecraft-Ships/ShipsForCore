package org.ships.vessel.common.requirement;

import org.jetbrains.annotations.NotNull;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;

public interface Requirement {

    boolean useOnStrict();

    void onCheckRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException;

    void onProcessRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException;

    @NotNull Requirement createChild();

    @NotNull Requirement createCopy();

    Optional<Requirement> getParent();

    boolean isEnabled();
}
