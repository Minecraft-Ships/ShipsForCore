package org.ships.movement.action;

import org.core.vector.type.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

class FixedAction implements MovementAction {

    private final @NotNull Supplier<Vector3<Integer>> vector;

    public FixedAction(@NotNull Supplier<Vector3<Integer>> vector) {
        this.vector = vector;
    }

    @Override
    public Vector3<Integer> move(@NotNull Vector3<Integer> position) {
        return position.plus(this.vector.get());
    }
}
