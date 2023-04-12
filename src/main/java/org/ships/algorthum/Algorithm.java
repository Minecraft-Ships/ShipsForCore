package org.ships.algorthum;

import org.core.TranslateCore;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.utils.Identifiable;
import org.jetbrains.annotations.ApiStatus;
import org.ships.config.node.DedicatedNode;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

public interface Algorithm extends Identifiable {

    @ApiStatus.Experimental
    Collection<DedicatedNode<?, ?, ? extends ConfigurationNode.KnownParser<?, ?>>> getNodes();

    @ApiStatus.Experimental
    default Optional<ConfigurationStream> configuration() {
        Optional<File> opFile = this.configurationFile();
        if (opFile.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(
                TranslateCore.createConfigurationFile(opFile.get(), TranslateCore.getPlatform().getConfigFormat()));
    }

    @ApiStatus.Experimental
    Optional<File> configurationFile();
}
