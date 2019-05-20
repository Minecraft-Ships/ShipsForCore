package org.ships.vessel.common.assits;

import org.core.configuration.ConfigurationFile;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.io.File;

public interface ClassicShip extends Vessel {

    ClassicShip deserializeClassicExtra(ConfigurationFile file);

    default File getClassicFile(){
        return new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "VesselData/" + getName() + ".temp");
    }
}
