package org.ships.vessel.structure;

import org.core.vector.types.Vector3Int;
import org.core.world.position.BlockPosition;
import org.core.world.position.ExactPosition;
import org.core.world.position.Positionable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface ShipsStructure {

    Set<Vector3Int> getRelitivePositions();

    default Collection<BlockPosition> getPositions(Positionable positionable){
        BlockPosition bPos = positionable.getPosition() instanceof BlockPosition ? (BlockPosition) positionable.getPosition() : ((ExactPosition)positionable.getPosition()).toBlockPosition();
        return getPositions(bPos);
    }

    default Collection<BlockPosition> getPositions(BlockPosition position){
        Set<BlockPosition> set = new HashSet<>();
        getRelitivePositions().stream().forEach(v -> set.add(position.getRelative(v)));
        return Collections.unmodifiableCollection(set);
    }
}
