package org.ships.vessel.structure;

import org.core.vector.types.Vector3Int;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.Positionable;
import org.core.world.position.impl.sync.SyncBlockPosition;

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

    default Collection<SyncBlockPosition> getPositions(Positionable<? extends Position<? extends Number>> positionable){
        return getPositions(Position.toBlock(positionable.getPosition()));
    }

    default Collection<SyncBlockPosition> getPositions(BlockPosition position){
        Set<SyncBlockPosition> set = new HashSet<>();
        getRelativePositions().forEach(v -> set.add(Position.toSync(position.getRelative(v))));
        return Collections.unmodifiableCollection(set);
    }
}
