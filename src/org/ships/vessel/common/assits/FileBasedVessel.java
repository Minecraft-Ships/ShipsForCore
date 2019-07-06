package org.ships.vessel.common.assits;

import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.io.File;
import java.util.Map;

public interface FileBasedVessel extends Vessel {

    File getFile();
    Map<ConfigurationNode, Object> serialize(ConfigurationFile file);
    AbstractShipsVessel deserializeExtra(ConfigurationFile file);


}
