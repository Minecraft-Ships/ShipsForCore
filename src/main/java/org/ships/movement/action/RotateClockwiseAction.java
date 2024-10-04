package org.ships.movement.action;

import org.core.vector.type.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

class RotateClockwiseAction implements MovementAction {

    private final Supplier<Vector3<Integer>> rotateAround;

    public RotateClockwiseAction(Supplier<Vector3<Integer>> supplier) {
        this.rotateAround = supplier;
    }

    @Override
    public Vector3<Integer> move(@NotNull Vector3<Integer> position) {
        Vector3<Integer> around = rotateAround.get();

        int symmetry = around.getX();
        int shift = symmetry - around.getZ();

        int x = position.getX() - (position.getX() - symmetry) * 2 - shift;
        int y = position.getY();
        int z = position.getZ() + shift;
        return Vector3.valueOf(z, y, x);
    }
}
