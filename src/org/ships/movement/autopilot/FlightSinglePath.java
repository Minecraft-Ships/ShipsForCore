package org.ships.movement.autopilot;

import org.core.vector.types.Vector3Int;

import java.util.List;

public interface FlightSinglePath extends FlightPath {

    List<Vector3Int> getLinedPath();
}
