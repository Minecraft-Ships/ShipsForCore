package org.ships.movement.action;

import org.core.vector.type.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

class RotateAntiClockwiseAction implements MovementAction {

    private final Supplier<Vector3<Integer>> rotateAround;

    public RotateAntiClockwiseAction(Supplier<Vector3<Integer>> supplier) {
        this.rotateAround = supplier;
    }

    @Override
    public Vector3<Integer> move(@NotNull Vector3<Integer> position) {
        Vector3<Integer> around = rotateAround.get();

        int symmetry = around.getX();
        int shift = symmetry - around.getZ();

        int z = position.getZ() - (position.getZ() - symmetry) * 2 - shift;
        int y = position.getY();
        int x = position.getX() - shift;
        return Vector3.valueOf(z, y, x);
    }
}
