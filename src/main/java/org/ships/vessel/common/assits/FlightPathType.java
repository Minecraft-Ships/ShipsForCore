package org.ships.vessel.common.assits;

import org.ships.movement.autopilot.FlightPath;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;

@Deprecated(forRemoval = true)
public interface FlightPathType extends Vessel {

    Optional<FlightPath> getFlightPath();

    FlightPathType setFlightPath(FlightPath path);
}
