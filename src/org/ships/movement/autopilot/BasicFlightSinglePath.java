package org.ships.movement.autopilot;

import org.core.vector.types.Vector3Int;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class BasicFlightSinglePath implements FlightSinglePath {

    protected Vector3Int firstPosition;
    protected Vector3Int secondPosition;

    public BasicFlightSinglePath(Vector3Int first, Vector3Int second){
        this.firstPosition = first;
        this.secondPosition = second;
    }

    @Override
    public Vector3Int getStartingPosition(){
        return this.firstPosition;
    }

    @Override
    public Vector3Int getEndingPosition(){
        return this.secondPosition;
    }

    @Override
    public FlightPath createUpdatedPath(Vector3Int from, Vector3Int to) {
        return new BasicFlightSinglePath(from, to);
    }

    @Override
    public List<FlightSinglePath> getPath(){
        return Collections.singletonList(this);
    }

    public boolean isUsingX(){
        return this.firstPosition.getZ().equals(this.secondPosition.getZ());
    }

    public boolean isUsingZ(){
        return this.firstPosition.getX().equals(this.secondPosition.getX());
    }

    public boolean isUsingY(){
        return !this.firstPosition.getY().equals(this.secondPosition.getY());
    }

    @Override
    public List<Vector3Int> getLinedPath(){
        List<Vector3Int> list = new ArrayList<>();
        if(isUsingY()){
            getLinedPath(Vector3Int::getY).forEach(i -> {
                Vector3Int vector = new Vector3Int(this.firstPosition.getX(), i, this.firstPosition.getZ());
                if(vector.equals(this.firstPosition)){
                    return;
                }
                list.add(vector);
            });
        }else if(isUsingX()) {
            getLinedPath(Vector3Int::getX).forEach(i -> {
                Vector3Int vector = new Vector3Int(i, this.firstPosition.getY(), this.firstPosition.getZ());
                if(vector.equals(this.firstPosition)){
                    return;
                }
                list.add(vector);
            });
        }else if(isUsingZ()){
            getLinedPath(Vector3Int::getZ).forEach(i -> {
                Vector3Int vector = new Vector3Int(this.firstPosition.getX(), this.firstPosition.getY(), i);
                if(vector.equals(this.firstPosition)){
                    return;
                }
                list.add(vector);
            });
        }
        return list;
    }

    private List<Integer> getLinedPath(Function<Vector3Int, Integer> function){
        List<Integer> list = new ArrayList<>();
        int pos1 = function.apply(this.firstPosition);
        int pos2 = function.apply(this.secondPosition);
        int direction = 1;
        if(pos1 > pos2){
            direction = -1;
        }
        int current = pos1;
        while(current != pos2){
            list.add(current);
            current = current + direction;
        }
        return list;
    }
}
