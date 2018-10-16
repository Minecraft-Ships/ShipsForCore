package org.ships.vessel.structure;

import org.core.vector.types.Vector3Int;
import org.core.world.position.BlockPosition;

import java.util.HashSet;
import java.util.Set;

public class AbstractPosititionableShipsStructure implements PositionableShipsStructure {

    Set<Vector3Int> vectors = new HashSet<>();
    BlockPosition position;

    public AbstractPosititionableShipsStructure(BlockPosition position){
        this.position = position;
    }

    @Override
    public BlockPosition getPosition() {
        return null;
    }

    @Override
    public Set<Vector3Int> getRelativePositions() {
        return null;
    }
}
