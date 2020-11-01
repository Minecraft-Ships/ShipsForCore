package org.ships.movement.autopilot;

import org.core.vector.type.Vector3;

import java.util.List;

public interface FlightSinglePath extends FlightPath {

    List<Vector3<Integer>> getLinedPath();
}
