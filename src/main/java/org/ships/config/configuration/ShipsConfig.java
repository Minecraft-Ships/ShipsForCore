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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ShipsConfig implements Config.CommandConfigurable {

    protected ConfigurationFile file;

    protected final ConfigurationNode ADVANCED_MOVEMENT = new ConfigurationNode("Advanced", "Movement", "Default");
    protected final ConfigurationNode ADVANCED_BLOCKFINDER = new ConfigurationNode("Advanced", "BlockFinder", "Default");
    protected final ConfigurationNode ADVANCED_TRACK_LIMIT = new ConfigurationNode("Advanced", "BlockFinder", "Track");
    protected final ConfigurationNode ADVANCED_MOVEMENT_STACK_LIMIT = new ConfigurationNode("Advanced", "Movement", "Stack", "Limit");
    protected final ConfigurationNode ADVANCED_MOVEMENT_STACK_DELAY = new ConfigurationNode("Advanced", "Movement", "Stack", "Delay");
    protected final ConfigurationNode ADVANCED_MOVEMENT_STACK_DELAYUNIT = new ConfigurationNode("Advanced", "Movement", "Stack", "DelayUnit");
    protected final ConfigurationNode ADVANCED_BLOCKFINDER_STACK_DELAY = new ConfigurationNode("Advanced", "BlockFinder", "Stack", "Delay");
    protected final ConfigurationNode ADVANCED_BLOCKFINDER_STACK_DELAYUNIT = new ConfigurationNode("Advanced", "BlockFinder", "Stack", "DelayUnit");
    protected final ConfigurationNode ADVANCED_BLOCKFINDER_STACK_LIMIT = new ConfigurationNode("Advanced", "BlockFinder", "Stack", "Limit");
    protected final ConfigurationNode EOT_DELAY = new ConfigurationNode("Auto", "EOT", "Delay");
    protected final ConfigurationNode EOT_DELAY_UNIT = new ConfigurationNode("Auto", "EOT", "DelayUnit");
    protected final ConfigurationNode EOT_SPEED = new ConfigurationNode("Auto", "EOT", "Speed");
    protected final ConfigurationNode EOT_ENABLED =  new ConfigurationNode("Auto", "EOT", "Enabled");
    protected final ConfigurationNode FALL_DELAY = new ConfigurationNode("Auto", "Falling", "Delay");
    protected final ConfigurationNode FALL_DELAY_UNIT = new ConfigurationNode("Auto", "Falling", "DelayUnit");
    protected final ConfigurationNode FALL_SPEED = new ConfigurationNode("Auto", "Falling", "Speed");
    protected final ConfigurationNode FALL_ENABLED = new ConfigurationNode("Auto", "Falling", "Enabled");
    protected final ConfigurationNode LICENCE_SIGN_TEXT_1ST = new ConfigurationNode("Sign", "Licence", "First");
    protected final ConfigurationNode LICENCE_SIGN_TEXT_4TH = new ConfigurationNode("Sign", "Licence", "Fourth");
    protected final ConfigurationNode VISIBLE_BOSS_BAR = new ConfigurationNode("Bar", "Visible");
    protected final ConfigurationNode STRUCTURE_UPDATE_AUTO = new ConfigurationNode("Structure", "Update", "Auto");
    protected final ConfigurationNode STRUCTURE_UPDATE_CLICK = new ConfigurationNode("Structure", "Update", "Click");

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
            this.file.set(this.ADVANCED_MOVEMENT, ShipsParsers.STRING_TO_MOVEMENT, BasicMovement.SHIPS_FIVE);
        }
        if(!this.file.parseBoolean(this.VISIBLE_BOSS_BAR).isPresent()){
            modified = true;
            this.file.set(this.VISIBLE_BOSS_BAR, false);
        }
        if(!this.file.parseBoolean(this.ALPHA_COMMAND_USE_LEGACY).isPresent()){
            modified = true;
            this.file.set(this.ALPHA_COMMAND_USE_LEGACY, true);
        }
        if(!this.file.parseInt(this.ADVANCED_MOVEMENT_STACK_LIMIT).isPresent()){
            modified = true;
            this.file.set(this.ADVANCED_MOVEMENT_STACK_LIMIT, 50);
            this.file.set(this.ADVANCED_MOVEMENT_STACK_DELAY, 1);
            this.file.set(this.ADVANCED_MOVEMENT_STACK_DELAYUNIT, Parser.STRING_TO_MINECRAFT_TIME_UNIT, TimeUnit.SECONDS);
            this.file.set(this.ADVANCED_BLOCKFINDER_STACK_DELAY, 1);
            this.file.set(this.ADVANCED_BLOCKFINDER_STACK_DELAYUNIT, Parser.STRING_TO_MINECRAFT_TIME_UNIT, TimeUnit.SECONDS);
            this.file.set(this.ADVANCED_BLOCKFINDER_STACK_LIMIT, 50);
            this.file.set(this.STRUCTURE_UPDATE_AUTO, true);
        }
        if(!this.file.parseBoolean(this.STRUCTURE_UPDATE_CLICK).isPresent()){
            modified = true;
            this.file.set(this.STRUCTURE_UPDATE_CLICK, false);
        }
        if (!this.file.parseBoolean(this.EOT_ENABLED).isPresent()){
            modified = true;
            this.file.set(this.EOT_ENABLED, true);
            this.file.set(this.EOT_DELAY_UNIT, Parser.STRING_TO_MINECRAFT_TIME_UNIT, TimeUnit.SECONDS);
            this.file.set(this.FALL_DELAY, 1);
            this.file.set(this.FALL_DELAY_UNIT, Parser.STRING_TO_MINECRAFT_TIME_UNIT, TimeUnit.MINUTES);
            this.file.set(this.FALL_SPEED, 1);
            this.file.set(this.FALL_ENABLED, true);
        }
        if(modified){
            this.file.save();
        }
        this.file.reload();
    }

    public boolean isFallingEnabled(){
        return this.file.parseBoolean(this.FALL_ENABLED).orElse(true);
    }

    public TimeUnit getFallingDelayUnit(){
        return this.file.parse(this.FALL_DELAY_UNIT, Parser.STRING_TO_MINECRAFT_TIME_UNIT).orElse(TimeUnit.MINUTES);
    }

    public int getFallingDelay(){
        return this.file.parseInt(this.FALL_DELAY).orElse(1);
    }

    public int getFallingSpeed(){
        return this.file.parseInt(this.FALL_SPEED).orElse(1);
    }

    public boolean isEOTEnabled(){
        return this.file.parseBoolean(this.EOT_ENABLED).orElse(true);
    }

    public TimeUnit getEOTDelayUnit() {
        return this.file.parse(this.EOT_DELAY_UNIT, Parser.STRING_TO_MINECRAFT_TIME_UNIT).orElse(TimeUnit.MINUTES);
    }

    public int getEOTDelay(){
        return this.file.parseInt(this.EOT_DELAY).orElse(5);
    }

    public int getEOTSpeed(){
        return this.file.parseInt(this.EOT_SPEED).orElse(2);
    }

    public int getDefaultFinderStackDelay(){
        return this.file.parseInt(this.ADVANCED_BLOCKFINDER_STACK_DELAY).orElse(1);
    }

    public TimeUnit getDefaultFinderStackDelayUnit(){
        Optional<TimeUnit> opTimeUnit = this.file.parse(this.ADVANCED_BLOCKFINDER_STACK_DELAYUNIT, Parser.STRING_TO_MINECRAFT_TIME_UNIT);
        if(opTimeUnit == null){
            return null;
        }
        return opTimeUnit.orElse(TimeUnit.SECONDS);
    }

    public TimeUnit getDefaultMovementStackDelayUnit(){
        Optional<TimeUnit> opTimeUnit = this.file.parse(this.ADVANCED_MOVEMENT_STACK_DELAYUNIT, Parser.STRING_TO_MINECRAFT_TIME_UNIT);
        if(opTimeUnit == null){
            return null;
        }
        return opTimeUnit.orElse(TimeUnit.SECONDS);
    }

    public boolean isStructureClickUpdating(){
        return this.file.parseBoolean(this.STRUCTURE_UPDATE_CLICK).orElse(false);
    }

    public boolean isStructureAutoUpdating(){
        return this.file.parseBoolean(this.STRUCTURE_UPDATE_AUTO).orElse(true);
    }

    public int getDefaultMovementStackLimit(){
        return this.file.parseInt(this.ADVANCED_MOVEMENT_STACK_LIMIT).orElse(100);
    }

    public int getDefaultFinderStackLimit(){
        return this.file.parseInt(this.ADVANCED_BLOCKFINDER_STACK_LIMIT).orElse(50);
    }

    public int getDefaultMovementStackDelay(){
        return this.file.parseInt(this.ADVANCED_MOVEMENT_STACK_DELAY).orElse(1);
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
        file.set(EOT_DELAY_UNIT, Parser.STRING_TO_MINECRAFT_TIME_UNIT, TimeUnit.SECONDS);
        file.set(EOT_ENABLED, true);
        file.set(FALL_SPEED, 1);
        file.set(FALL_DELAY, 1);
        file.set(FALL_DELAY_UNIT, Parser.STRING_TO_MINECRAFT_TIME_UNIT, TimeUnit.MINUTES);
        file.set(FALL_ENABLED, true);
        file.set(VISIBLE_BOSS_BAR, false);
        file.set(this.ADVANCED_MOVEMENT_STACK_LIMIT, 50);
        file.set(this.ADVANCED_MOVEMENT_STACK_DELAY, 1);
        file.set(this.ADVANCED_MOVEMENT_STACK_DELAYUNIT, Parser.STRING_TO_MINECRAFT_TIME_UNIT, TimeUnit.SECONDS);
        file.set(this.ADVANCED_BLOCKFINDER_STACK_DELAY, 1);
        file.set(this.ADVANCED_BLOCKFINDER_STACK_DELAYUNIT, Parser.STRING_TO_MINECRAFT_TIME_UNIT, TimeUnit.SECONDS);
        file.set(this.ADVANCED_BLOCKFINDER_STACK_LIMIT, 50);
        file.set(this.STRUCTURE_UPDATE_AUTO, true);
        file.set(this.STRUCTURE_UPDATE_CLICK, false);

        file.set(ALPHA_COMMAND_USE_LEGACY, true);
        file.save();
    }

    @Override
    public Set<DedicatedNode<?>> getNodes() {
        return new HashSet<>(Arrays.asList(
                new DedicatedNode<>(true, "Boss.Bar.Visible", Parser.STRING_TO_BOOLEAN, VISIBLE_BOSS_BAR.getPath()),
                new DedicatedNode<>("Advanced.Block.Movement", ShipsParsers.STRING_TO_MOVEMENT, ADVANCED_MOVEMENT.getPath()),
                new DedicatedNode<>("Advanced.Block.Finder", ShipsParsers.STRING_TO_BLOCK_FINDER, ADVANCED_BLOCKFINDER.getPath()),
                new DedicatedNode<>(true, "Advanced.Block.Track", Parser.STRING_TO_INTEGER, ADVANCED_TRACK_LIMIT.getPath()),
                new DedicatedNode<>("Advanced.Block.Movement.Stack.DelayUnit", Parser.STRING_TO_MINECRAFT_TIME_UNIT, ADVANCED_MOVEMENT_STACK_DELAYUNIT.getPath()),
                new DedicatedNode<>(true, "Advanced.Block.Movement.Stack.Delay", Parser.STRING_TO_INTEGER, ADVANCED_MOVEMENT_STACK_DELAY.getPath()),
                new DedicatedNode<>(true, "Advanced.Block.Movement.Stack.Limit", Parser.STRING_TO_INTEGER, ADVANCED_MOVEMENT_STACK_LIMIT.getPath()),
                new DedicatedNode<>(true, "Advanced.Block.Finder.Stack.Delay", Parser.STRING_TO_INTEGER, ADVANCED_BLOCKFINDER_STACK_DELAY.getPath()),
                new DedicatedNode<>("Advanced.Block.Finder.Stack.DelayUnit", Parser.STRING_TO_MINECRAFT_TIME_UNIT, ADVANCED_BLOCKFINDER_STACK_DELAYUNIT.getPath()),
                new DedicatedNode<>(true, "Advanced.Block.Finder.Stack.Limit", Parser.STRING_TO_INTEGER, ADVANCED_BLOCKFINDER_STACK_LIMIT.getPath()),
                new DedicatedNode<>(true, "Structure.Auto.Update", Parser.STRING_TO_BOOLEAN, STRUCTURE_UPDATE_AUTO.getPath()),
                new DedicatedNode<>(true, "Running.EOT.Enabled", Parser.STRING_TO_BOOLEAN, EOT_ENABLED.getPath()),
                new DedicatedNode<>(true, "Running.EOT.Delay", Parser.STRING_TO_INTEGER, EOT_DELAY.getPath()),
                new DedicatedNode<>("Running.EOT.DelayUnit", Parser.STRING_TO_MINECRAFT_TIME_UNIT, EOT_DELAY_UNIT.getPath()),
                new DedicatedNode<>(true, "Running.EOT.Speed", Parser.STRING_TO_INTEGER, EOT_SPEED.getPath()),
                new DedicatedNode<>(true, "Running.Fall.Enabled", Parser.STRING_TO_BOOLEAN, FALL_ENABLED.getPath()),
                new DedicatedNode<>(true, "Running.Fall.Delay", Parser.STRING_TO_INTEGER, FALL_DELAY.getPath()),
                new DedicatedNode<>("Running.Fall.DelayUnit", Parser.STRING_TO_MINECRAFT_TIME_UNIT, FALL_DELAY_UNIT.getPath()),
                new DedicatedNode<>(true, "Running.Fall.Speed", Parser.STRING_TO_INTEGER, FALL_SPEED.getPath())
        ));
    }
}
