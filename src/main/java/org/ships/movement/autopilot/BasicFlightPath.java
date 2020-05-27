package org.ships.movement.autopilot;

import org.core.source.viewer.CommandViewer;
import org.core.vector.types.Vector3Int;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BasicFlightPath implements FlightPath {

    protected Vector3Int first;
    protected Vector3Int second;
    protected CommandViewer viewer;

    public BasicFlightPath(Vector3Int first, Vector3Int second){
        this.first = first;
        this.second = second;
    }

    @Override
    public Optional<CommandViewer> getViewer(){
        return Optional.ofNullable(this.viewer);
    }

    @Override
    public BasicFlightPath setViewer(CommandViewer viewer){
        this.viewer = viewer;
        return this;
    }

    @Override
    public List<FlightSinglePath> getPath() {
        List<FlightSinglePath> list = new ArrayList<>();
        list.add((FlightSinglePath) new BasicFlightSinglePath(this.first, new Vector3Int(this.first.getX(), this.second.getY(), this.first.getZ())).setViewer(this.viewer));
        list.add((FlightSinglePath) new BasicFlightSinglePath(new Vector3Int(this.first.getX(), this.second.getY(), this.first.getZ()), new Vector3Int(this.second.getX(), this.second.getY(), this.first.getZ())).setViewer(this.viewer));
        list.add((FlightSinglePath) new BasicFlightSinglePath(new Vector3Int(this.second.getX(), this.second.getY(), this.first.getZ()), this.second).setViewer(this.viewer));
        return list;
    }

    @Override
    public Vector3Int getStartingPosition() {
        return this.first;
    }

    @Override
    public Vector3Int getEndingPosition() {
        return this.second;
    }

    @Override
    public FlightPath createUpdatedPath(Vector3Int from, Vector3Int to) {
        return new BasicFlightPath(from, to);
    }
}
