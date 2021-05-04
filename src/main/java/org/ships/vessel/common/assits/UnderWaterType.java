package org.ships.vessel.common.assits;

import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.HashSet;
import java.util.Set;

public interface UnderWaterType extends WaterType {

    default boolean isSubmerged() {
        PositionableShipsStructure pss = getStructure();
        Direction[] directions = FourFacingDirection.getFourFacingDirections();
        int height = pss.getYSize();
        Set<Integer> values = new HashSet<>();
        for (SyncBlockPosition position : pss.getPositions()) {
            if (values.contains(position.getY())) {
                continue;
            }
            for (Direction direction : directions) {
                BlockType type = position.getRelative(direction).getBlockType();
                if (type.isLike(BlockTypes.WATER)) {
                    values.add(position.getY());
                }
            }
        }
        Integer lowest = null;
        Integer highest = null;
        for (int value : values) {
            if (lowest == null) {
                lowest = value;
                highest = value;
                continue;
            }
            if (lowest > value) {
                lowest = value;
            }
            if (highest < value) {
                highest = value;
            }
        }
        if (highest == null) {
            return false;
        }
        return (highest - lowest) == height;
    }
}
