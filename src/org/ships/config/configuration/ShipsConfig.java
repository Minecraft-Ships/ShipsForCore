package org.ships.config.configuration;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.ships.algorthum.blockfinder.BasicBlockFinder;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.Config;
import org.ships.config.parsers.ShipsParsers;
import org.ships.plugin.ShipsPlugin;

import java.io.File;

public class ShipsConfig implements Config {

    protected ConfigurationFile file;

    protected final ConfigurationNode ADVANCED_MOVEMENT = new ConfigurationNode("Advanced", "Movement", "Default");
    protected final ConfigurationNode ADVANCED_BLOCKFINDER = new ConfigurationNode("Advanced", "BlockFinder", "Default");

    public ShipsConfig(){
        File file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "Configuration/Config.temp");
        this.file = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.DEFAULT);
        if(!this.file.getFile().exists()){
            recreateFile();
        }
    }

    public BasicBlockFinder getDefaultFinder(){
        return this.file.parse(this.ADVANCED_BLOCKFINDER, ShipsParsers.STRING_TO_BLOCK_FINDER).orElse(BasicBlockFinder.SHIPS_FIVE);
    }

    public BasicMovement getDefaultMovement(){
        return this.file.parse(this.ADVANCED_MOVEMENT, ShipsParsers.STRING_TO_MOVEMENT).orElse(BasicMovement.SHIPS_FIVE);
    }

    @Override
    public org.core.configuration.ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public void recreateFile() {
        ConfigurationFile file = getFile();
        file.set(ADVANCED_MOVEMENT, ShipsParsers.STRING_TO_MOVEMENT, BasicMovement.SHIPS_FIVE);
        file.set(ADVANCED_BLOCKFINDER, ShipsParsers.STRING_TO_BLOCK_FINDER, BasicBlockFinder.SHIPS_FIVE);
    }
}
