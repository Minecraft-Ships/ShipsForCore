package org.ships.vessel.common.loader;

import org.ships.exceptions.load.LoadVesselException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.IdentifiableShip;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;

@Deprecated(forRemoval = true)
public class ShipsIDFinder implements ShipsLoader {

    protected final String id;

    public ShipsIDFinder(String id) {
        this.id = id.toLowerCase();
    }

    @Override
    public Vessel load() throws LoadVesselException {
        Optional<Vessel> opVessel = ShipsPlugin
                .getPlugin()
                .getVessels()
                .stream()
                .filter(v -> v instanceof IdentifiableShip)
                .filter(v -> {
                    try {
                        String id = ((IdentifiableShip) v).getId();
                        if (id.equals(this.id)) {
                            return true;
                        }
                        if (!id.startsWith("ships:")) {
                            return false;
                        }
                        return id.substring(6).equals(this.id);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .findFirst();
        if (opVessel.isPresent()) {
            return opVessel.get();
        }
        throw new LoadVesselException("Cannot find " + this.id);
    }
}
