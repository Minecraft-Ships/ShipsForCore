package org.ships.movement.autopilot;

import org.core.source.viewer.CommandViewer;
import org.core.vector.type.Vector3;

import java.util.List;
import java.util.Optional;

/**
 * @deprecated Will be replaced with completely rewritten AutoPilot code
 */
@Deprecated(forRemoval = true)
public interface FlightPath {

    List<FlightSinglePath> getPath();
    Vector3<Integer> getStartingPosition();
    Vector3<Integer> getEndingPosition();
    FlightPath createUpdatedPath(Vector3<Integer> from, Vector3<Integer> to);
    Optional<CommandViewer> getViewer();
    FlightPath setViewer(CommandViewer viewer);

    default FlightPath removeViewer(){
        return this.setViewer(null);
    }

    default Optional<Vector3<Integer>> getNext(){
        return this.getNext(0);
    }

    default Optional<Vector3<Integer>> getNext(int B){
        List<FlightSinglePath> list = this.getPath();
        if(list.isEmpty()){
            return Optional.empty();
        }
        for (FlightSinglePath flightSinglePath : list) {
            List<Vector3<Integer>> vectors = flightSinglePath.getLinedPath();
            if (vectors.isEmpty()) {
                continue;
            }
            return Optional.of(vectors.get(B));
        }
        return Optional.empty();
    }

}
