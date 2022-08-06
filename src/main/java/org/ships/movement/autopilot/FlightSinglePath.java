package org.ships.movement.autopilot;

import org.core.vector.type.Vector3;

import java.util.List;

/**
 * @deprecated Will be replaced with completely rewritten AutoPilot code
 */
@Deprecated(forRemoval = true)
public interface FlightSinglePath extends FlightPath {

    List<Vector3<Integer>> getLinedPath();
}
