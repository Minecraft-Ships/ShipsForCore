package org.ships.movement.autopilot;

import org.core.source.viewer.CommandViewer;
import org.core.vector.type.Vector3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class BasicFlightSinglePath implements FlightSinglePath {

    protected final Vector3<Integer> firstPosition;
    protected final Vector3<Integer> secondPosition;
    protected CommandViewer viewer;

    public BasicFlightSinglePath(Vector3<Integer> first, Vector3<Integer> second) {
        this.firstPosition = first;
        this.secondPosition = second;
    }

    @Override
    public Vector3<Integer> getStartingPosition() {
        return this.firstPosition;
    }

    @Override
    public Vector3<Integer> getEndingPosition() {
        return this.secondPosition;
    }

    @Override
    public FlightPath createUpdatedPath(Vector3<Integer> from, Vector3<Integer> to) {
        return new BasicFlightSinglePath(from, to);
    }

    @Override
    public Optional<CommandViewer> getViewer() {
        return Optional.ofNullable(this.viewer);
    }

    @Override
    public FlightPath setViewer(CommandViewer viewer) {
        this.viewer = viewer;
        return this;
    }

    @Override
    public List<FlightSinglePath> getPath() {
        return Collections.singletonList(this);
    }

    public boolean isUsingX() {
        return this.firstPosition.getZ().equals(this.secondPosition.getZ());
    }

    public boolean isUsingZ() {
        return this.firstPosition.getX().equals(this.secondPosition.getX());
    }

    public boolean isUsingY() {
        return !this.firstPosition.getY().equals(this.secondPosition.getY());
    }

    @Override
    public List<Vector3<Integer>> getLinedPath() {
        List<Vector3<Integer>> list = new ArrayList<>();
        if (this.isUsingY()) {
            this.getLinedPath(Vector3::getY).forEach(i -> {
                Vector3<Integer> vector = Vector3.valueOf(this.firstPosition.getX(), i, this.firstPosition.getZ());
                if (vector.equals(this.firstPosition)) {
                    return;
                }
                list.add(vector);
            });
        } else if (this.isUsingX()) {
            this.getLinedPath(Vector3::getX).forEach(i -> {
                Vector3<Integer> vector = Vector3.valueOf(i, this.firstPosition.getY(), this.firstPosition.getZ());
                if (vector.equals(this.firstPosition)) {
                    return;
                }
                list.add(vector);
            });
        } else if (this.isUsingZ()) {
            this.getLinedPath(Vector3::getZ).forEach(i -> {
                Vector3<Integer> vector = Vector3.valueOf(this.firstPosition.getX(), this.firstPosition.getY(), i);
                if (vector.equals(this.firstPosition)) {
                    return;
                }
                list.add(vector);
            });
        }
        return list;
    }

    private List<Integer> getLinedPath(Function<? super Vector3<Integer>, Integer> function) {
        List<Integer> list = new ArrayList<>();
        int pos1 = function.apply(this.firstPosition);
        int pos2 = function.apply(this.secondPosition);
        int direction = 1;
        if (pos1 > pos2) {
            direction = -1;
        }
        int current = pos1;
        while (current!=pos2) {
            list.add(current);
            current = current + direction;
        }
        return list;
    }
}
