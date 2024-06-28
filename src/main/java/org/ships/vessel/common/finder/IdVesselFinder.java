package org.ships.vessel.common.finder;

import org.ships.exceptions.load.LoadVesselException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.IdentifiableShip;
import org.ships.vessel.common.types.Vessel;

public final class IdVesselFinder {

    private IdVesselFinder() {
        throw new RuntimeException("Do not create");
    }

    public static Vessel load(String id) throws LoadVesselException {
        return ShipsPlugin.getPlugin().getVessels().stream().filter(v -> v instanceof IdentifiableShip).filter(v -> {
            try {
                String vesselId = ((IdentifiableShip) v).getId();
                if (id.equals(vesselId)) {
                    return true;
                }
                if (!id.startsWith("ships:")) {
                    return false;
                }
                return vesselId.substring(6).equals(id);
            } catch (Throwable e) {
                e.printStackTrace();
                return false;
            }
        }).findFirst().orElseThrow(() -> new LoadVesselException("cannot find " + id));
    }

}
