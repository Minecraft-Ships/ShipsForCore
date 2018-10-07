package org.ships.vessel.common.types;

import org.core.world.position.BlockPosition;
import org.core.world.position.Positionable;
import org.ships.vessel.structure.PositionableShipsStructure;

public interface Vessel extends Positionable {

    String getName();
    PositionableShipsStructure getStructure();
    
    @Override
    BlockPosition getPosition();

}
