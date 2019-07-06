package org.ships.config.configuration;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.ships.algorthum.blockfinder.BasicBlockFinder;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.Config;
import org.ships.config.node.DedicatedNode;
import org.ships.config.parsers.ShipsParsers;
import org.ships.plugin.ShipsPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ShipsConfig implements Config.CommandConfigurable {

    protected ConfigurationFile file;

    protected final ConfigurationNode ADVANCED_MOVEMENT = new ConfigurationNode("Advanced", "Movement", "Default");
    protected final ConfigurationNode ADVANCED_BLOCKFINDER = new ConfigurationNode("Advanced", "BlockFinder", "Default");
    protected final ConfigurationNode ADVANCED_TRACK_LIMIT = new ConfigurationNode("Advanced", "BlockFinder", "Track");
    protected final ConfigurationNode EOT_DELAY = new ConfigurationNode("Auto", "EOT", "Delay");
    protected final ConfigurationNode EOT_DELAY_UNIT = new ConfigurationNode("Auto", "EOT", "DelayUnit");
    protected final ConfigurationNode EOT_SPEED = new ConfigurationNode("Auto", "EOT", "Speed");
    protected final ConfigurationNode FALL_DELAY = new ConfigurationNode("Auto", "Falling", "Delay");
    protected final ConfigurationNode FALL_SPEED = new ConfigurationNode("Auto", "Falling", "Speed");
    protected final ConfigurationNode LICENCE_SIGN_TEXT_1ST = new ConfigurationNode("Sign", "Licence", "First");
    protected final ConfigurationNode LICENCE_SIGN_TEXT_4TH = new ConfigurationNode("Sign", "Licence", "Fourth");

    public ShipsConfig(){
        File file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "Configuration/Config.temp");
        this.file = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.DEFAULT);
        if(!this.file.getFile().exists()){
            recreateFile();
        }
    }

    public int getEOTDelay(){
        return this.file.parseInt(this.EOT_DELAY).orElse(5);
    }

    public int getEOTSpeed(){
        return this.file.parseInt(this.EOT_SPEED).orElse(2);
    }

    public BasicBlockFinder getDefaultFinder(){
        return this.file.parse(this.ADVANCED_BLOCKFINDER, ShipsParsers.STRING_TO_BLOCK_FINDER).orElse(BasicBlockFinder.SHIPS_FIVE);
    }

    public BasicMovement getDefaultMovement(){
        return this.file.parse(this.ADVANCED_MOVEMENT, ShipsParsers.STRING_TO_MOVEMENT).orElse(BasicMovement.SHIPS_FIVE);
        //return BasicMovement.SHIPS_SIX;
    }

    @Override
    public org.core.configuration.ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public void recreateFile() {
        ConfigurationFile file = getFile();
        //file.set(ADVANCED_MOVEMENT, ShipsParsers.STRING_TO_MOVEMENT, BasicMovement.SHIPS_FIVE);
        file.set(ADVANCED_BLOCKFINDER, ShipsParsers.STRING_TO_BLOCK_FINDER, BasicBlockFinder.SHIPS_FIVE);
        file.set(EOT_SPEED, 2);
        file.set(EOT_DELAY, 5);
        file.save();
    }

    @Override
    public Set<DedicatedNode<?>> getNodes() {
        return new HashSet<>(Arrays.asList(
                new DedicatedNode<>("Advanced.Block.Finder", ShipsParsers.STRING_TO_BLOCK_FINDER, ADVANCED_BLOCKFINDER.getPath())
        ));
    }
}
