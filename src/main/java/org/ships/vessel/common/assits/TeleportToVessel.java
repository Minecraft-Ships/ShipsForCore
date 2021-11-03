package org.ships.vessel.common.assits;

import org.core.vector.type.Vector3;
import org.core.world.position.impl.ExactPosition;
import org.ships.vessel.common.types.Vessel;

import java.util.Map;

public interface TeleportToVessel extends Vessel {

    Map<String, ExactPosition> getTeleportPositions();

    Map<String, Vector3<Double>> getTeleportVectors();

    TeleportToVessel setTeleportPosition(ExactPosition position, String id);

    TeleportToVessel setTeleportVector(Vector3<Double> position, String id);

    default TeleportToVessel setTeleportPosition(ExactPosition position) {
        return this.setTeleportPosition(position, "default");
    }
}
