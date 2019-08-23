package org.ships.vessel.structure;

import org.core.vector.types.Vector3Int;
import org.core.world.position.BlockPosition;
import org.core.world.position.ExactPosition;
import org.core.world.position.Positionable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public interface ShipsStructure {

    Set<Vector3Int> getRelativePositions();
    Set<Vector3Int> getOriginalRelativePositions();
    boolean addPosition(Vector3Int add);
    boolean removePosition(Vector3Int remove);
    ShipsStructure clear();
    ShipsStructure setRaw(Collection<Vector3Int> collection);

    default int getXSize(){
        return getSpecificSize(v -> v.getX());
    }

    default int getYSize(){
        return getSpecificSize(v -> v.getY());
    }

    default int getZSize(){
        return getSpecificSize(v -> v.getZ());
    }

    default int getSpecificSize(Function<Vector3Int, Integer> function){
        Integer min = null;
        Integer max = null;
        for(Vector3Int vector : getRelativePositions()){
            int value = function.apply(vector);
            if(min == null && max == null){
                max = value;
                min = value;
                continue;
            }
            if(min > value){
                min = value;
            }
            if(max > value){
                max = value;
            }
        }
        if(min == 0 && max == 0){
            return 0;
        }
        return max - min;
    }

    default Collection<BlockPosition> getPositions(Positionable positionable){
        BlockPosition bPos = positionable.getPosition() instanceof BlockPosition ? (BlockPosition) positionable.getPosition() : ((ExactPosition)positionable.getPosition()).toBlockPosition();
        return getPositions(bPos);
    }

    default Collection<BlockPosition> getPositions(BlockPosition position){
        Set<BlockPosition> set = new HashSet<>();
        getRelativePositions().stream().forEach(v -> set.add(position.getRelative(v)));
        return Collections.unmodifiableCollection(set);
    }
}
