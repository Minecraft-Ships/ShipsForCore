package org.ships.vessel.common.loader.shipsvessel;

import org.ships.exceptions.load.LoadVesselException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsLoader;
import org.ships.vessel.common.types.typical.ShipsVessel;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

public class ShipsIDLoader implements ShipsLoader {

    protected String id;

    public ShipsIDLoader(String id){
        this.id = id;
        if(this.id.startsWith("ships.")){
            this.id = id.substring(6);
        }
    }

    @Override
    public ShipsVessel load() throws LoadVesselException {
        String[] id = this.id.split(":");
        File[] folder = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "VesselData/ships." + id[0]).listFiles();
        if(folder == null){
            throw new LoadVesselException("No ShipType of " + id[0] + " found");
        }
        Optional<File> opFile = Stream.of(folder).filter(f -> f.getName().toLowerCase().startsWith(id[1])).findFirst();
        if(!opFile.isPresent()){
            throw new LoadVesselException("Can not find " + this.id);
        }
        return new ShipsFileLoader(opFile.get()).load();
    }
}
