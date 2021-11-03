package org.ships.vessel.structure;

import org.core.vector.type.Vector3;
import org.core.world.position.Positionable;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.sync.SyncBlockPosition;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public interface ShipsStructure {

    Set<Vector3<Integer>> getRelativePositions();
    Set<Vector3<Integer>> getOriginalRelativePositions();
    boolean addPosition(Vector3<Integer> add);
    boolean removePosition(Vector3<Integer> remove);
    ShipsStructure clear();
    ShipsStructure setRaw(Collection<? extends Vector3<Integer>> collection);

    default int getXSize(){
        return this.getSpecificSize(Vector3::getX);
    }

    default int getYSize(){
        return this.getSpecificSize(Vector3::getY);
    }

    default int getZSize(){
        return this.getSpecificSize(Vector3::getZ);
    }

    default int getSpecificSize(Function<? super Vector3<Integer>, Integer> function){
        Integer min = null;
        Integer max = null;
        for(Vector3<Integer> vector : this.getRelativePositions()){
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
        return this.getPositions(Position.toBlock(positionable.getPosition()));
    }

    default Collection<SyncBlockPosition> getPositions(BlockPosition position){
        Set<SyncBlockPosition> set = new HashSet<>();
        this.getRelativePositions().forEach(v -> set.add(Position.toSync(position.getRelative(v))));
        return Collections.unmodifiableCollection(set);
    }
}
