package org.ships.vessel.common.assits;

import org.core.world.position.ExactPosition;
import org.ships.vessel.common.types.Vessel;

public interface TeleportToVessel extends Vessel {

    ExactPosition getTeleportPosition();
    TeleportToVessel setTeleportPosition(ExactPosition position);
}
