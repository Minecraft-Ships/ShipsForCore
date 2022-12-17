package org.ships.vessel.common.assits;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.jetbrains.annotations.NotNull;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.util.Map;

public interface FileBasedVessel extends Vessel {

    @NotNull File getFile();

    @NotNull Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(@NotNull ConfigurationStream file);

    @NotNull FileBasedVessel deserializeExtra(@NotNull ConfigurationStream file);


}
