package org.ships.config.messages;

import org.core.CorePlugin;
import org.core.adventureText.AText;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.config.parser.StringParser;
import org.ships.config.Config;
import org.ships.config.node.DedicatedNode;
import org.ships.config.node.ObjectDedicatedNode;
import org.ships.plugin.ShipsPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Deprecated
public class MessageConfig implements Config.KnownNodes {

    private final ConfigurationStream.ConfigurationFile file;

    private static final ConfigurationNode.KnownParser.SingleKnown<String> TOO_MANY = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_STRING_PARSER, "Error", "TooManyOfBlocks");
    private static final ConfigurationNode.KnownParser.SingleKnown<String> NO_SPEED_SET = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_STRING_PARSER, "Error", "NoSpeedSet");
    private static final ConfigurationNode.KnownParser.SingleKnown<String> FAILED_TO_FIND_LICENCE = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_STRING_PARSER, "Error", "FailedToFindLicenceSign");
    private static final ConfigurationNode.KnownParser.SingleKnown<String> NO_SPECIAL_BLOCK_FOUND = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_STRING_PARSER, "Error", "NoSpecialBlockFound");
    private static final ConfigurationNode.KnownParser.SingleKnown<String> NO_SPECIAL_NAMED_BLOCK_FOUND = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_STRING_PARSER, "Error", "NoSpecialNamedBlockFound");
    private static final ConfigurationNode.KnownParser.SingleKnown<String> NOT_IN_MOVING_IN = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_STRING_PARSER, "Error", "NotInMovingIn");
    private static final ConfigurationNode.KnownParser.SingleKnown<String> SYNCED_SHIP = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_STRING_PARSER, "Error", "SyncedShip");

    private static final ConfigurationNode.KnownParser.SingleKnown<AText> ALTITUDE_FIRST_LINE = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_TEXT, "Sign", "Altitude", "First");
    private static final ConfigurationNode.KnownParser.SingleKnown<AText> ALTITUDE_SECOND_LINE = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_TEXT, "Sign", "Altitude", "First");

    public MessageConfig() {
        File file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "Configuration/Messages." + CorePlugin.getPlatform().getConfigFormat().getFileType()[0]);
        this.file = CorePlugin.createConfigurationFile(file, CorePlugin.getPlatform().getConfigFormat());
        boolean modifications = false;
        if (!this.file.getString(TOO_MANY).isPresent()) {
            modifications = true;
            recreateFile();
        }
        if (!this.file.getString(SYNCED_SHIP).isPresent()) {
            modifications = true;
            this.file.set(SYNCED_SHIP, StringParser.STRING_TO_STRING_PARSER, "Ship has resynced");
        }
        if (modifications) {
            this.file.save();
        }
        this.file.reload();
    }

    public Set<String> getSuggestions(ConfigurationNode node) {
        Set<String> set = new HashSet<>();
        set.add("%Vessel Name%");
        set.add("%Vessel Id%");
        set.add("%Player Name%");
        if (node.equals(NOT_IN_MOVING_IN)) {
            set.add("%Block Names%");
            set.add("%Block Ids%");
        } else if (node.equals(NO_SPECIAL_NAMED_BLOCK_FOUND)) {
            set.add("%Block Name%");
        } else if (node.equals(NO_SPECIAL_BLOCK_FOUND)) {
            set.add("%Block Name%");
            set.add("%Block Id%");
        } else if (node.equals(FAILED_TO_FIND_LICENCE)) {
            return new HashSet<>();
        } else if (node.equals(NO_SPEED_SET)) {
            //TODO
        } else if (node.equals(TOO_MANY)) {
            set.add("%Block Name%");
            set.add("%Block Id%");
        }
        return set;
    }

    @Deprecated
    public String getNotInMovingIn() {
        return this.file.getString(NOT_IN_MOVING_IN, "Must be moving into one of the following blocks: %Block Names%");
    }

    @Deprecated
    public String getSyncedShip() {
        return this.file.getString(SYNCED_SHIP, "Ship has resynced");
    }

    @Deprecated
    public String getTooManyBlocks() {
        return this.file.getString(TOO_MANY, "Too many of %Block Name% Found");
    }

    @Deprecated
    public String getNoSpeedSet() {
        return this.file.getString(NO_SPEED_SET, "No Speed Set");
    }

    @Deprecated
    public String getFailedToFindLicenceSign() {
        return this.file.getString(FAILED_TO_FIND_LICENCE, "Failed to find licence sign");
    }

    @Deprecated
    public String getFailedToFindSpecialBlock() {
        return this.file.getString(NO_SPECIAL_BLOCK_FOUND, "Failed to find %Block Name%");
    }

    @Deprecated
    public String getFailedToFindNamedSpecialBlock() {
        return this.file.getString(NO_SPECIAL_NAMED_BLOCK_FOUND, "Failed to find %Block Name%");
    }

    @Override
    public ConfigurationStream.ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public void recreateFile() {
        this.file.set(NO_SPEED_SET, StringParser.STRING_TO_STRING_PARSER, "No Speed Set");
        this.file.set(FAILED_TO_FIND_LICENCE, StringParser.STRING_TO_STRING_PARSER, "Failed to Find Licence Sign");
        this.file.set(NO_SPECIAL_BLOCK_FOUND, StringParser.STRING_TO_STRING_PARSER, "Failed to find %Block Name%");
        this.file.set(NO_SPECIAL_NAMED_BLOCK_FOUND, StringParser.STRING_TO_STRING_PARSER, "Failed to find %Block Name%");
        this.file.set(NOT_IN_MOVING_IN, StringParser.STRING_TO_STRING_PARSER, "Must be moving into one of the following blocks: %Block Names%");
        this.file.save();
    }

    @Override
    public Set<DedicatedNode<?, ?, ?>> getNodes() {
        Set<DedicatedNode<?, ?, ?>> set = new HashSet<>();
        set.add(new ObjectDedicatedNode<>(TOO_MANY, "Error.TooManyBlocks"));
        set.add(new ObjectDedicatedNode<>(NO_SPEED_SET, "Error.NoSpeedSet"));
        set.add(new ObjectDedicatedNode<>(FAILED_TO_FIND_LICENCE, "Error.FailedToFindLicenceSign"));
        set.add(new ObjectDedicatedNode<>(NO_SPECIAL_BLOCK_FOUND, "Error.NoSpecialBlock"));
        set.add(new ObjectDedicatedNode<>(NO_SPECIAL_NAMED_BLOCK_FOUND, "Error.NoSpecialNamedBlock"));
        set.add(new ObjectDedicatedNode<>(NOT_IN_MOVING_IN, "Error.NotMovingInto"));
        set.add(new ObjectDedicatedNode<>(SYNCED_SHIP, "Error.SyncedShip"));
        return set;
    }
}
