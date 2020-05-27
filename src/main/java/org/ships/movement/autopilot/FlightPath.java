package org.ships.movement.autopilot;

import org.core.source.viewer.CommandViewer;
import org.core.vector.types.Vector3Int;

import java.util.List;
import java.util.Optional;

public interface FlightPath {

    List<FlightSinglePath> getPath();
    Vector3Int getStartingPosition();
    Vector3Int getEndingPosition();
    FlightPath createUpdatedPath(Vector3Int from, Vector3Int to);
    Optional<CommandViewer> getViewer();
    FlightPath setViewer(CommandViewer viewer);

    default FlightPath removeViewer(){
        return this.setViewer(null);
    }

    default Optional<Vector3Int> getNext(){
        return getNext(0);
    }

    default Optional<Vector3Int> getNext(int B){
        List<FlightSinglePath> list = getPath();
        if(list.isEmpty()){
            return Optional.empty();
        }
        for (FlightSinglePath flightSinglePath : list) {
            List<Vector3Int> vectors = flightSinglePath.getLinedPath();
            if (vectors.isEmpty()) {
                continue;
            }
            return Optional.of(vectors.get(B));
        }
        return Optional.empty();
    }

}
