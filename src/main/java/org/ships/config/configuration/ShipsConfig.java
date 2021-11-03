package org.ships.config.configuration;

import org.array.utils.ArrayUtils;
import org.core.TranslateCore;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.schedule.unit.TimeUnit;
import org.core.world.WorldExtent;
import org.ships.algorthum.blockfinder.BasicBlockFinder;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.Config;
import org.ships.config.node.CollectionDedicatedNode;
import org.ships.config.node.DedicatedNode;
import org.ships.config.node.ObjectDedicatedNode;
import org.ships.config.node.RawDedicatedNode;
import org.ships.config.parsers.ShipsParsers;
import org.ships.plugin.ShipsPlugin;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ShipsConfig implements Config.KnownNodes {

    protected ConfigurationStream.ConfigurationFile file;

    protected final ObjectDedicatedNode<BasicMovement, ConfigurationNode.KnownParser.SingleKnown<BasicMovement>> ADVANCED_MOVEMENT = new ObjectDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(ShipsParsers.STRING_TO_MOVEMENT, "Advanced", "Movement", "Default"), "Advanced.Block.Movement");
    protected final ObjectDedicatedNode<BasicBlockFinder, ConfigurationNode.KnownParser.SingleKnown<BasicBlockFinder>> ADVANCED_BLOCKFINDER = new ObjectDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(ShipsParsers.STRING_TO_BLOCK_FINDER, "Advanced", "BlockFinder", "Default"), "Advanced.Block.Finder");
    protected final RawDedicatedNode<Integer, ConfigurationNode.KnownParser.SingleKnown<Integer>> ADVANCED_TRACK_LIMIT = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Advanced", "BlockFinder", "Track"), "Advanced.Block.Track", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final RawDedicatedNode<Integer, ConfigurationNode.KnownParser.SingleKnown<Integer>> ADVANCED_MOVEMENT_STACK_LIMIT = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Advanced", "Movement", "Stack", "Limit"), "Advanced.Block.Movement.Stack.Limit", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final RawDedicatedNode<Integer, ConfigurationNode.KnownParser.SingleKnown<Integer>> ADVANCED_MOVEMENT_STACK_DELAY = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Advanced", "Movement", "Stack", "Delay"), "Advanced.Block.Movement.Stack.Delay", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final ObjectDedicatedNode<TimeUnit, ConfigurationNode.KnownParser.SingleKnown<TimeUnit>> ADVANCED_MOVEMENT_STACK_DELAYUNIT = new ObjectDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_MINECRAFT_TIME_UNIT, "Advanced", "Movement", "Stack", "DelayUnit"), "Advanced.Block.Movement.Stack.DelayUnit");
    protected final RawDedicatedNode<Integer, ConfigurationNode.KnownParser.SingleKnown<Integer>> ADVANCED_BLOCKFINDER_STACK_DELAY = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Advanced", "BlockFinder", "Stack", "Delay"), "Advanced.Block.Finder.Stack.Delay", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final ObjectDedicatedNode<TimeUnit, ConfigurationNode.KnownParser.SingleKnown<TimeUnit>> ADVANCED_BLOCKFINDER_STACK_DELAYUNIT = new ObjectDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_MINECRAFT_TIME_UNIT, "Advanced", "BlockFinder", "Stack", "DelayUnit"), "Advanced.Block.Finder.Stack.DelayUnit");
    protected final RawDedicatedNode<Integer, ConfigurationNode.KnownParser.SingleKnown<Integer>> ADVANCED_BLOCKFINDER_STACK_LIMIT = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Advanced", "BlockFinder", "Stack", "Limit"), "Advanced.Block.Finder.Stack.Limit", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final RawDedicatedNode<Integer, ConfigurationNode.KnownParser.SingleKnown<Integer>> ADVANCED_ENTITYFINDER_STACK_LIMIT = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Advanced", "EntityFinder", "Stack", "Limit"), "Advanced.Entity.Finder.Stack.Limit", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final RawDedicatedNode<Integer, ConfigurationNode.KnownParser.SingleKnown<Integer>> EOT_DELAY = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Auto", "EOT", "Delay"), "Running.EOT.Delay", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final ObjectDedicatedNode<TimeUnit, ConfigurationNode.KnownParser.SingleKnown<TimeUnit>> EOT_DELAY_UNIT = new ObjectDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_MINECRAFT_TIME_UNIT, "Auto", "EOT", "DelayUnit"), "Running.EOT.DelayUnit");
    protected final RawDedicatedNode<Integer, ConfigurationNode.KnownParser.SingleKnown<Integer>> EOT_SPEED = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Auto", "EOT", "Speed"), "Running.EOT.Speed", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final RawDedicatedNode<Boolean, ConfigurationNode.KnownParser.SingleKnown<Boolean>> EOT_ENABLED = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_BOOLEAN, "Auto", "EOT", "Enabled"), "Running.EOT.Enabled", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final RawDedicatedNode<Integer, ConfigurationNode.KnownParser.SingleKnown<Integer>> FALL_DELAY = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Auto", "Falling", "Delay"), "Running.Fall.Delay", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final ObjectDedicatedNode<TimeUnit, ConfigurationNode.KnownParser.SingleKnown<TimeUnit>> FALL_DELAY_UNIT = new ObjectDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_MINECRAFT_TIME_UNIT, "Auto", "Falling", "DelayUnit"), "Running.Fall.DelayUnit");
    protected final RawDedicatedNode<Integer, ConfigurationNode.KnownParser.SingleKnown<Integer>> FALL_SPEED = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Auto", "Falling", "Speed"), "Running.Fall.Speed", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final RawDedicatedNode<Boolean, ConfigurationNode.KnownParser.SingleKnown<Boolean>> FALL_ENABLED = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_BOOLEAN, "Auto", "Falling", "Enabled"), "Running.Fall.Enabled", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final ObjectDedicatedNode<String, ConfigurationNode.KnownParser.SingleKnown<String>> LICENCE_SIGN_TEXT_1ST = new ObjectDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_STRING_PARSER, "Sign", "Licence", "First"), "sign.licence.first");
    protected final ObjectDedicatedNode<String, ConfigurationNode.KnownParser.SingleKnown<String>> LICENCE_SIGN_TEXT_4TH = new ObjectDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_STRING_PARSER, "Sign", "Licence", "Fourth"), "sign.licence.fourth");
    protected final RawDedicatedNode<Boolean, ConfigurationNode.KnownParser.SingleKnown<Boolean>> VISIBLE_BOSS_BAR = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_BOOLEAN, "Bar", "Visible"), "Boss.Bar.Visible", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final RawDedicatedNode<Boolean, ConfigurationNode.KnownParser.SingleKnown<Boolean>> STRUCTURE_UPDATE_AUTO = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_BOOLEAN, "Structure", "Update", "Auto"), "Structure.Auto.Update", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final RawDedicatedNode<Boolean, ConfigurationNode.KnownParser.SingleKnown<Boolean>> STRUCTURE_UPDATE_CLICK = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_BOOLEAN, "Structure", "Update", "Click"), "Structure.Click.Update", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final RawDedicatedNode<Boolean, ConfigurationNode.KnownParser.SingleKnown<Boolean>> MOVEMENT_REQUIREMENTS_CHECK_MAX_BLOCK_TYPE = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_BOOLEAN, "Movement", "Requirements", "Check", "Max", "BlockType"), "Movement.Requirements.Check.Max.BlockType", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final RawDedicatedNode<Boolean, ConfigurationNode.KnownParser.SingleKnown<Boolean>> UPDATE_ENABLED = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_BOOLEAN, "Update", "Enabled"), "Update.Enabled", (f, v) -> f.set(v.getKey(), v.getValue()));
    protected final CollectionDedicatedNode<WorldExtent, Set<WorldExtent>,
            ConfigurationNode.KnownParser.CollectionKnown<WorldExtent>> DISABLED_WORLDS = new CollectionDedicatedNode<>(new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_WORLD, "World", "Disabled"), "worlds.ignore");
    protected final ObjectDedicatedNode<String, ConfigurationNode.KnownParser.SingleKnown<String>> LOGIN_COMMAND = new ObjectDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_STRING_PARSER, "Login", "Command"), "login.command");
    protected final ObjectDedicatedNode<Integer, ConfigurationNode.KnownParser.SingleKnown<Integer>> SIGN_MOVE_SPEED = new ObjectDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Sign", "Move", "Speed"), "sign.move.speed");

    @Deprecated
    public final RawDedicatedNode<Boolean, ConfigurationNode.KnownParser.SingleKnown<Boolean>> ALPHA_COMMAND_USE_LEGACY = new RawDedicatedNode<>(new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_BOOLEAN, "AlphaOnly", "Command", "UseLegacy"), "Alpha.Commands.Legacy", (f, v) -> f.set(v.getKey(), v.getValue()));

    public ShipsConfig() {
        File file = new File(ShipsPlugin.getPlugin().getConfigFolder(), "Configuration/Config." + TranslateCore.getPlatform().getConfigFormat().getFileType()[0]);
        this.file = TranslateCore.createConfigurationFile(file, TranslateCore.getPlatform().getConfigFormat());
        boolean modified = false;
        if (!this.file.getFile().exists()) {
            this.recreateFile();
        }
        if (!this.file.parse(this.ADVANCED_MOVEMENT.getNode()).isPresent()) {
            modified = true;
            this.file.set(this.ADVANCED_MOVEMENT.getNode(), BasicMovement.SHIPS_SIX);
        }
        if (!this.file.getBoolean(this.VISIBLE_BOSS_BAR.getNode()).isPresent()) {
            modified = true;
            this.file.set(this.VISIBLE_BOSS_BAR.getNode(), true);
        }
        if (!this.file.getInteger(this.ADVANCED_MOVEMENT_STACK_LIMIT.getNode()).isPresent()) {
            modified = true;
            this.file.set(this.ADVANCED_MOVEMENT_STACK_LIMIT.getNode(), 10);
            this.file.set(this.ADVANCED_MOVEMENT_STACK_DELAY.getNode(), 1);
            this.file.set(this.ADVANCED_MOVEMENT_STACK_DELAYUNIT.getNode(), TimeUnit.MINECRAFT_TICKS);
            this.file.set(this.ADVANCED_BLOCKFINDER_STACK_DELAY.getNode(), 1);
            this.file.set(this.ADVANCED_BLOCKFINDER_STACK_DELAYUNIT.getNode(), TimeUnit.MINECRAFT_TICKS);
            this.file.set(this.ADVANCED_BLOCKFINDER_STACK_LIMIT.getNode(), 7);
            this.file.set(this.STRUCTURE_UPDATE_AUTO.getNode(), true);
        }
        if (!this.file.getBoolean(this.STRUCTURE_UPDATE_CLICK.getNode()).isPresent()) {
            modified = true;
            this.file.set(this.STRUCTURE_UPDATE_CLICK.getNode(), false);
        }
        if (!this.file.getBoolean(this.EOT_ENABLED.getNode()).isPresent()) {
            modified = true;
            this.file.set(this.EOT_ENABLED.getNode(), false);
            this.file.set(this.EOT_DELAY_UNIT.getNode(), TimeUnit.SECONDS);
            this.file.set(this.FALL_DELAY.getNode(), 1);
            this.file.set(this.FALL_DELAY_UNIT.getNode(), TimeUnit.MINUTES);
            this.file.set(this.FALL_SPEED.getNode(), 1);
            this.file.set(this.FALL_ENABLED.getNode(), false);
        }
        if (!this.file.getBoolean(this.MOVEMENT_REQUIREMENTS_CHECK_MAX_BLOCK_TYPE.getNode()).isPresent()) {
            modified = true;
            this.file.set(this.MOVEMENT_REQUIREMENTS_CHECK_MAX_BLOCK_TYPE.getNode(), false);
        }
        if (!this.file.getInteger(this.ADVANCED_ENTITYFINDER_STACK_LIMIT.getNode()).isPresent()) {
            modified = true;
            this.file.set(this.ADVANCED_ENTITYFINDER_STACK_LIMIT.getNode(), 75);
        }
        if (!this.file.getBoolean(this.UPDATE_ENABLED.getNode()).isPresent()) {
            modified = true;
            this.file.set(this.UPDATE_ENABLED.getNode(), true);
            this.file.set(this.DISABLED_WORLDS.getNode(), Collections.emptySet());
        }
        if (!this.file.getString(this.LOGIN_COMMAND.getNode()).isPresent()) {
            modified = true;
            this.file.set(new ConfigurationNode(this.LOGIN_COMMAND.getNode().getPath()), "");
        }
        if (!this.file.getInteger(this.SIGN_MOVE_SPEED.getNode()).isPresent()) {
            modified = true;
            this.file.set(new ConfigurationNode(this.SIGN_MOVE_SPEED.getNode().getPath()), 2);
        }
        if (modified) {
            this.file.save();
        }
        this.file.reload();
    }

    public Set<WorldExtent> getDisabledWorlds() {
        return this.file.parseCollection(this.DISABLED_WORLDS.getNode(), new HashSet<>());
    }

    public boolean isUpdateEnabled() {
        return this.file.getBoolean(this.UPDATE_ENABLED.getNode(), true);
    }

    public boolean isFallingEnabled() {
        return this.file.getBoolean(this.FALL_ENABLED.getNode(), true);
    }

    public TimeUnit getFallingDelayUnit() {
        return this.file.parse(this.FALL_DELAY_UNIT.getNode(), TimeUnit.MINECRAFT_TICKS);
    }

    public int getDefaultMoveSpeed() {
        return this.file.getInteger(this.SIGN_MOVE_SPEED.getNode(), 2);
    }

    public int getFallingDelay() {
        return this.file.getInteger(this.FALL_DELAY.getNode(), 1);
    }

    public int getFallingSpeed() {
        return this.file.getInteger(this.FALL_SPEED.getNode(), 1);
    }

    public boolean isEOTEnabled() {
        return this.file.getBoolean(this.EOT_ENABLED.getNode(), false);
    }

    public boolean isMovementRequirementsCheckMaxBlockType() {
        return this.file.getBoolean(this.MOVEMENT_REQUIREMENTS_CHECK_MAX_BLOCK_TYPE.getNode(), false);
    }

    public TimeUnit getEOTDelayUnit() {
        return this.file.parse(this.EOT_DELAY_UNIT.getNode()).orElse(TimeUnit.SECONDS);
    }

    public int getEOTDelay() {
        return this.file.getInteger(this.EOT_DELAY.getNode(), 5);
    }

    public int getEOTSpeed() {
        return this.file.getInteger(this.EOT_SPEED.getNode(), 2);
    }

    public int getEntityTrackingLimit() {
        return this.file.getInteger(this.ADVANCED_ENTITYFINDER_STACK_LIMIT.getNode(), 75);
    }

    public int getDefaultFinderStackDelay() {
        return this.file.getInteger(this.ADVANCED_BLOCKFINDER_STACK_DELAY.getNode(), 1);
    }

    public TimeUnit getDefaultFinderStackDelayUnit() {
        return this.file.parse(this.ADVANCED_BLOCKFINDER_STACK_DELAYUNIT.getNode(), TimeUnit.MINECRAFT_TICKS);
    }

    public TimeUnit getDefaultMovementStackDelayUnit() {
        return this.file.parse(this.ADVANCED_MOVEMENT_STACK_DELAYUNIT.getNode(), TimeUnit.MINECRAFT_TICKS);
    }

    public boolean isStructureClickUpdating() {
        return this.file.getBoolean(this.STRUCTURE_UPDATE_CLICK.getNode(), false);
    }

    public boolean isStructureAutoUpdating() {
        return this.file.getBoolean(this.STRUCTURE_UPDATE_AUTO.getNode(), true);
    }

    public int getDefaultMovementStackLimit() {
        return this.file.getInteger(this.ADVANCED_MOVEMENT_STACK_LIMIT.getNode(), 7);
    }

    public int getDefaultFinderStackLimit() {
        return this.file.getInteger(this.ADVANCED_BLOCKFINDER_STACK_LIMIT.getNode(), 2);
    }

    public int getDefaultMovementStackDelay() {
        return this.file.getInteger(this.ADVANCED_MOVEMENT_STACK_DELAY.getNode(), 1);
    }

    public Optional<String> getDefaultLoginCommand() {
        String command = this.file.getString(this.LOGIN_COMMAND.getNode(), "");
        if (command.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(command);
    }

    public BasicBlockFinder getDefaultFinder() {
        return this.file.parse(this.ADVANCED_BLOCKFINDER.getNode(), BasicBlockFinder.SHIPS_FIVE).init();
    }

    public BasicMovement getDefaultMovement() {
        return this.file.parse(this.ADVANCED_MOVEMENT.getNode(), BasicMovement.SHIPS_SIX);
    }

    public boolean isBossBarVisible() {
        return this.file.getBoolean(this.VISIBLE_BOSS_BAR.getNode(), true);
    }

    public int getDefaultTrackSize() {
        return this.file.getInteger(this.ADVANCED_TRACK_LIMIT.getNode(), 4000);
    }

    @Override
    public ConfigurationStream.ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public void recreateFile() {
        File file = this.getFile().getFile();
        boolean exist = file.exists();
        if (!file.delete() && exist) {
            throw new IllegalStateException("Failed to create the config. Something went wrong");
        }
        Optional<ConfigurationStream.ConfigurationFile> opConfig = ShipsPlugin.getPlugin().createConfig("Config.yml", file);
        if (opConfig.isPresent()) {
            this.file = opConfig.get();
        } else {
            throw new IllegalStateException("Failed to create the config. Something went wrong");
        }
    }

    @Override
    public Set<DedicatedNode<?, ?, ? extends ConfigurationNode.KnownParser<?, ?>>> getNodes() {
        return ArrayUtils.ofSet(
                this.ADVANCED_BLOCKFINDER,
                this.ADVANCED_BLOCKFINDER_STACK_DELAY,
                this.ADVANCED_BLOCKFINDER_STACK_DELAYUNIT,
                this.ADVANCED_BLOCKFINDER_STACK_LIMIT,
                this.ADVANCED_ENTITYFINDER_STACK_LIMIT,
                this.ADVANCED_MOVEMENT,
                this.ADVANCED_MOVEMENT_STACK_DELAY,
                this.ADVANCED_MOVEMENT_STACK_DELAYUNIT,
                this.ADVANCED_MOVEMENT_STACK_LIMIT,
                this.ADVANCED_TRACK_LIMIT,
                this.DISABLED_WORLDS,
                this.EOT_DELAY,
                this.EOT_DELAY_UNIT,
                this.EOT_SPEED,
                this.EOT_ENABLED,
                this.FALL_DELAY,
                this.FALL_DELAY_UNIT,
                this.FALL_ENABLED,
                this.FALL_SPEED,
                this.LICENCE_SIGN_TEXT_1ST,
                this.LICENCE_SIGN_TEXT_4TH,
                this.MOVEMENT_REQUIREMENTS_CHECK_MAX_BLOCK_TYPE,
                this.STRUCTURE_UPDATE_AUTO,
                this.STRUCTURE_UPDATE_CLICK,
                this.VISIBLE_BOSS_BAR,
                this.UPDATE_ENABLED
        );
    }
}
