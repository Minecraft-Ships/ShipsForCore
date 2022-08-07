package org.ships.vessel.common.assits;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.util.Map;

public interface FileBasedVessel extends Vessel {

    File getFile();

    Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(ConfigurationStream file);

    FileBasedVessel deserializeExtra(ConfigurationStream file);


}
