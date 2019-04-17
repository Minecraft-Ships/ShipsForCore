package org.ships.vessel.common.loader;

import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.AbstractShipsVessel;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

public class ShipsIDLoader implements ShipsLoader {

    protected String id;

    public ShipsIDLoader(String id){
        this.id = id;
    }

    @Override
    public AbstractShipsVessel load() throws IOException {
        String[] id = this.id.split(":");
        File[] folder = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "VesselData/ships." + id[0]).listFiles();
        if(folder == null){
            throw new IOException("No ShipType of " + id[0] + " found");
        }
        Optional<File> opFile = Stream.of(folder).filter(f -> f.getName().toLowerCase().startsWith(id[1])).findFirst();
        if(!opFile.isPresent()){
            throw new IOException("Can not find " + this.id);
        }
        System.out.println("Path: " + opFile.get().getAbsolutePath());
        return new ShipsFileLoader(opFile.get()).load();
    }
}
