package org.ships.config.configuration;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
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
    protected final ConfigurationNode VISIBLE_BOSS_BAR = new ConfigurationNode("Bar", "Visible");

    @Deprecated
    public final ConfigurationNode ALPHA_COMMAND_USE_LEGACY = new ConfigurationNode("AlphaOnly", "Command", "UseLegacy");

    public ShipsConfig(){
        File file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "Configuration/Config.temp");
        this.file = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.DEFAULT);
        boolean modified = false;
        if(!this.file.getFile().exists()){
            recreateFile();
        }
        if(!this.file.parse(this.ADVANCED_MOVEMENT, ShipsParsers.STRING_TO_MOVEMENT).isPresent()){
            modified = true;
            this.file.set(ADVANCED_MOVEMENT, ShipsParsers.STRING_TO_MOVEMENT, BasicMovement.SHIPS_FIVE);
        }
        if(!this.file.parseBoolean(this.VISIBLE_BOSS_BAR).isPresent()){
            modified = true;
            this.file.set(this.VISIBLE_BOSS_BAR, false);
        }
        if(!this.file.parseBoolean(this.ALPHA_COMMAND_USE_LEGACY).isPresent()){
            modified = true;
            this.file.set(this.ALPHA_COMMAND_USE_LEGACY, true);
        }
        if(modified){
            this.file.save();
        }
        this.file.reload();
    }

    public int getEOTDelay(){
        return this.file.parseInt(this.EOT_DELAY).orElse(5);
    }

    public int getEOTSpeed(){
        return this.file.parseInt(this.EOT_SPEED).orElse(2);
    }

    public BasicBlockFinder getDefaultFinder(){
        return this.file.parse(this.ADVANCED_BLOCKFINDER, ShipsParsers.STRING_TO_BLOCK_FINDER).orElse(BasicBlockFinder.SHIPS_FIVE).init();
    }

    public BasicMovement getDefaultMovement(){
        return this.file.parse(this.ADVANCED_MOVEMENT, ShipsParsers.STRING_TO_MOVEMENT).orElse(BasicMovement.SHIPS_FIVE);
    }

    public boolean isBossBarVisible(){
        return this.file.parseBoolean(this.VISIBLE_BOSS_BAR).orElse(false);
    }

    public int getDefaultTrackSize(){
        return this.file.parseInt(this.ADVANCED_TRACK_LIMIT).orElse(4000);
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
        file.set(ADVANCED_TRACK_LIMIT, 4000);
        file.set(EOT_SPEED, 2);
        file.set(EOT_DELAY, 5);
        file.set(VISIBLE_BOSS_BAR, false);

        file.set(ALPHA_COMMAND_USE_LEGACY, true);
        file.save();
    }

    @Override
    public Set<DedicatedNode<?>> getNodes() {
        return new HashSet<>(Arrays.asList(
                new DedicatedNode<>(true, "boss.bar.visible", Parser.STRING_TO_BOOLEAN, VISIBLE_BOSS_BAR.getPath()),
                new DedicatedNode<>("Advanced.Block.Movement", ShipsParsers.STRING_TO_MOVEMENT, ADVANCED_MOVEMENT.getPath()),
                new DedicatedNode<>("Advanced.Block.Finder", ShipsParsers.STRING_TO_BLOCK_FINDER, ADVANCED_BLOCKFINDER.getPath()),
                new DedicatedNode<>(true, "Advanced.Block.Track", Parser.STRING_TO_INTEGER, ADVANCED_TRACK_LIMIT.getPath())
        ));
    }
}
