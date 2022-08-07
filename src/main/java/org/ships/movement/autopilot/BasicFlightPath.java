package org.ships.movement.autopilot;

import org.core.source.viewer.CommandViewer;
import org.core.vector.type.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @deprecated Will be replaced with completely rewritten AutoPilot code
 */

@Deprecated(forRemoval = true)
public class BasicFlightPath implements FlightPath {

    protected final Vector3<Integer> first;
    protected final Vector3<Integer> second;
    protected CommandViewer viewer;

    public BasicFlightPath(Vector3<Integer> first, Vector3<Integer> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public Optional<CommandViewer> getViewer() {
        return Optional.ofNullable(this.viewer);
    }

    @Override
    public BasicFlightPath setViewer(CommandViewer viewer) {
        this.viewer = viewer;
        return this;
    }

    @Override
    public List<FlightSinglePath> getPath() {
        List<FlightSinglePath> list = new ArrayList<>();
        list.add((FlightSinglePath) new BasicFlightSinglePath(this.first,
                Vector3.valueOf(this.first.getX(), this.second.getY(), this.first.getZ())).setViewer(this.viewer));
        list.add((FlightSinglePath) new BasicFlightSinglePath(
                Vector3.valueOf(this.first.getX(), this.second.getY(), this.first.getZ()),
                Vector3.valueOf(this.second.getX(), this.second.getY(), this.first.getZ())).setViewer(this.viewer));
        list.add((FlightSinglePath) new BasicFlightSinglePath(
                Vector3.valueOf(this.second.getX(), this.second.getY(), this.first.getZ()), this.second).setViewer(
                this.viewer));
        return list;
    }

    @Override
    public Vector3<Integer> getStartingPosition() {
        return this.first;
    }

    @Override
    public Vector3<Integer> getEndingPosition() {
        return this.second;
    }

    @Override
    public FlightPath createUpdatedPath(Vector3<Integer> from, Vector3<Integer> to) {
        return new BasicFlightPath(from, to);
    }
}
