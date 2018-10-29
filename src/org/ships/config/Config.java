package org.ships.config;

import org.core.configuration.ConfigurationFile;

public interface Config {

    public ConfigurationFile getFile();
    public void recreateFile();
}
