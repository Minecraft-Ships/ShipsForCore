package org.ships.vessel.structure;

import org.core.vector.type.Vector3;
import org.core.world.position.Positionable;
import org.core.world.position.impl.BlockPosition;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Deprecated
public interface ShipsStructure {

    Collection<Vector3<Integer>> getRelativePositions();

    Collection<Vector3<Integer>> getOriginalRelativePositions();

    boolean addPosition(Vector3<Integer> add);

    boolean removePosition(Vector3<Integer> remove);

    ShipsStructure clear();

    ShipsStructure setRaw(Collection<? extends Vector3<Integer>> collection);

    default int getXSize() {
        return this.getSpecificSize(Vector3::getX);
    }

    default int getYSize() {
        return this.getSpecificSize(Vector3::getY);
    }

    default int getZSize() {
        return this.getSpecificSize(Vector3::getZ);
    }

    default int getSpecificSize(Function<? super Vector3<Integer>, Integer> function) {
        Integer min = null;
        Integer max = null;
        for (Vector3<Integer> vector : this.getRelativePositions()) {
            int value = function.apply(vector);
            if (min == null && max == null) {
                max = value;
                min = value;
                continue;
            }
            if (min > value) {
                min = value;
            }
            if (max > value) {
                max = value;
            }
        }
        if (min == 0 && max == 0) {
            return 0;
        }
        return max - min;
    }

    @Deprecated(forRemoval = true)
    default <T extends BlockPosition> Collection<T> getPositions(Positionable<? extends T> positionable) {
        return this.getPositions(positionable.getPosition());
    }

    @Deprecated(forRemoval = true)
    default <T extends BlockPosition> Collection<T> getPositions(T position) {
        return this.getPositionsRelativeTo(position);
    }

    default <T extends BlockPosition> Collection<T> getPositionsRelativeTo(Positionable<? extends T> positionable) {
        return this.getPositionsRelativeTo(positionable.getPosition());
    }

    default <T extends BlockPosition> Collection<T> getPositionsRelativeTo(T position) {
        return this
                .getRelativePositions()
                .stream()
                .map(vector -> (T) position.getRelative(vector))
                .collect(Collectors.toUnmodifiableSet());
    }
}
