package org.ships.vessel.common.assits;

import org.core.world.position.impl.sync.SyncExactPosition;
import org.ships.vessel.common.types.Vessel;

import java.util.Map;

public interface TeleportToVessel extends Vessel {

    Map<String, SyncExactPosition> getTeleportPositions();

    @Deprecated
    default SyncExactPosition getTeleportPosition(){
        return getTeleportPositions().getOrDefault("Default", this.getPosition().toExactPosition());
    }

    @Deprecated
    default TeleportToVessel setTeleportPosition(SyncExactPosition position){
        getTeleportPositions().put("Default", position);
        return this;
    }
}
