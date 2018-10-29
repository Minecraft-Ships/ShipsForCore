package org.ships.vessel.structure;

import org.core.vector.types.Vector3Int;
import org.core.world.position.BlockPosition;

import java.util.HashSet;
import java.util.Set;

public class AbstractPosititionableShipsStructure implements PositionableShipsStructure {

    protected Set<Vector3Int> vectors = new HashSet<>();
    protected BlockPosition position;

    public AbstractPosititionableShipsStructure(BlockPosition position){
        this.position = position;
    }

    @Override
    public BlockPosition getPosition() {
        return this.position;
    }

    @Override
    public Set<Vector3Int> getRelativePositions() {
        return this.vectors;
    }

    @Override
    public boolean addPosition(Vector3Int add) {
        if(this.vectors.stream().anyMatch(v -> v.equals(add))){
            return false;
        }
        return this.vectors.add(add);
    }

    @Override
    public boolean removePosition(Vector3Int remove) {
        if(!this.vectors.stream().anyMatch(v -> v.equals(remove))){
            return false;
        }
        return this.vectors.remove(remove);
    }
}
