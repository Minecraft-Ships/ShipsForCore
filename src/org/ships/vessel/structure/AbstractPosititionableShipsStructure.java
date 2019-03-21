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
    public PositionableShipsStructure setPosition(BlockPosition pos) {
        this.position = pos;
        return this;
    }

    @Override
    public Set<Vector3Int> getRelativePositions() {
        Set<Vector3Int> vectors = new HashSet<>(getOriginalRelativePositions());
        if (!vectors.stream().anyMatch(v -> v.equals(new Vector3Int(0, 0, 0)))){
            vectors.add(new Vector3Int(0, 0, 0));
        }
        return vectors;
    }

    @Override
    public Set<Vector3Int> getOriginalRelativePositions() {
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
