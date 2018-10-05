package org.ships.vessel.common.types;

import org.core.world.position.BlockPosition;
import org.core.world.position.Positionable;

public interface Vessel extends Positionable {

    String getName();

    @Override
    BlockPosition getPosition();

}
