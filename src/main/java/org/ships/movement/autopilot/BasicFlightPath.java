package org.ships.movement.autopilot;

import org.core.vector.types.Vector3Int;

import java.util.ArrayList;
import java.util.List;

public class BasicFlightPath implements FlightPath {

    protected Vector3Int first;
    protected Vector3Int second;

    public BasicFlightPath(Vector3Int first, Vector3Int second){
        this.first = first;
        this.second = second;
    }

    @Override
    public List<FlightSinglePath> getPath() {
        List<FlightSinglePath> list = new ArrayList<>();
        list.add(new BasicFlightSinglePath(this.first, new Vector3Int(this.first.getX(), this.second.getY(), this.first.getZ())));
        list.add(new BasicFlightSinglePath(new Vector3Int(this.first.getX(), this.second.getY(), this.first.getZ()), new Vector3Int(this.second.getX(), this.second.getY(), this.first.getZ())));
        list.add(new BasicFlightSinglePath(new Vector3Int(this.second.getX(), this.second.getY(), this.first.getZ()), this.second));
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
