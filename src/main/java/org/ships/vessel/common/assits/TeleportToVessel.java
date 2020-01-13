package org.ships.vessel.common.assits;

import org.core.world.position.ExactPosition;
import org.ships.vessel.common.types.Vessel;

import java.util.Map;

public interface TeleportToVessel extends Vessel {

    Map<String, ExactPosition> getTeleportPositions();

    @Deprecated
    default ExactPosition getTeleportPosition(){
        return getTeleportPositions().getOrDefault("Default", this.getPosition().toExactPosition());
    }

    @Deprecated
    default TeleportToVessel setTeleportPosition(ExactPosition position){
        getTeleportPositions().put("Default", position);
        return this;
    }
}
