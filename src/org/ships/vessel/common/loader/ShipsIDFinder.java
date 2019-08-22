package org.ships.vessel.common.loader;

import org.core.utils.Identifable;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;

public class ShipsIDFinder implements ShipsLoader {

    protected String id;

    public ShipsIDFinder(String id){
        this.id = id.toLowerCase();
    }

    @Override
    public Vessel load() throws LoadVesselException {
        Optional<Vessel> opVessel = ShipsPlugin.getPlugin().getVessels().stream().filter(v -> v instanceof Identifable).filter(v -> {
            try {
                String id = ((Identifable) v).getId();
                if (id.equals(this.id)) {
                    return true;
                }
                if (!id.startsWith("ships:")) {
                    return false;
                }
                return id.substring(6).equals(this.id);
            }catch (Throwable e){
                e.printStackTrace();
                return false;
            }
        }).findFirst();
        if(opVessel.isPresent()){
            return opVessel.get();
        }
        throw new LoadVesselException("Can not find " + this.id);
    }
}
