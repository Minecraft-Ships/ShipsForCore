package org.ships.movement.action;

import org.core.vector.type.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface MovementAction {

    Vector3<Integer> move(@NotNull Vector3<Integer> position);

    static MovementAction rotateClockwise(Supplier<Vector3<Integer>> rotateAround) {
        return new RotateClockwiseAction(rotateAround);
    }

    static MovementAction rotateAntiClockwise(Supplier<Vector3<Integer>> rotateAround) {
        return new RotateAntiClockwiseAction(rotateAround);
    }

    static MovementAction plus(Supplier<Vector3<Integer>> plus) {
        return new FixedAction(plus);
    }

    static MovementAction plus(Vector3<Integer> plus) {
        return plus(() -> plus);
    }

}
