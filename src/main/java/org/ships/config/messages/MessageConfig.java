package org.ships.config.messages;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.ships.config.Config;
import org.ships.config.node.DedicatedNode;
import org.ships.plugin.ShipsPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class MessageConfig implements Config.CommandConfigurable {

    ConfigurationFile file;

    private static final ConfigurationNode TOO_MANY = new ConfigurationNode("Error", "TooManyOfBlocks");
    private static final ConfigurationNode NO_SPEED_SET = new ConfigurationNode("Error", "NoSpeedSet");
    private static final ConfigurationNode FAILED_TO_FIND_LICENCE = new ConfigurationNode("Error", "FailedToFindLicenceSign");
    private static final ConfigurationNode NO_SPECIAL_BLOCK_FOUND = new ConfigurationNode("Error", "NoSpecialBlockFound");
    private static final ConfigurationNode NO_SPECIAL_NAMED_BLOCK_FOUND = new ConfigurationNode("Error", "NoSpecialNamedBlockFound");

    public MessageConfig(){
        File file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "Configuration/Messages.temp");
        this.file = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.DEFAULT);
        boolean modifications = false;
        if (!this.file.parseString(TOO_MANY).isPresent()){
            recreateFile();
        }
        if (modifications){
            this.file.save();
        }
        this.file.reload();
    }

    public String getTooManyBlocks(){
        return this.file.parseString(TOO_MANY).orElse("Too many of %Block Name% Found");
    }

    public String getNoSpeedSet(){
        return this.file.parseString(NO_SPEED_SET).orElse("No Speed Set");
    }

    public String getFailedToFindLicenceSign(){
        return this.file.parseString(FAILED_TO_FIND_LICENCE).orElse("Failed to find licence sign");
    }

    public String getFailedToFindSpecialBlock(){
        return this.file.parseString(NO_SPECIAL_BLOCK_FOUND).orElse("Failed to find %Block Name%");
    }

    public String getFailedToFindNamedSpecialBlock(){
        return this.file.parseString(NO_SPECIAL_NAMED_BLOCK_FOUND).orElse("Failed to find %Block Name%");
    }

    @Override
    public ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public void recreateFile() {
        this.file.set(TOO_MANY, "Too many of %Block Name% Found");
        this.file.set(NO_SPEED_SET, "No Speed Set");
        this.file.set(FAILED_TO_FIND_LICENCE, "Failed to Find Licence Sign");
        this.file.set(NO_SPECIAL_BLOCK_FOUND, "Failed to find %Block Name%");
        this.file.set(NO_SPECIAL_NAMED_BLOCK_FOUND, "Failed to find %Block Name%");
        this.file.save();
    }

    @Override
    public Set<DedicatedNode<?>> getNodes() {
        Set<DedicatedNode<?>> set = new HashSet<>();
        set.add(new DedicatedNode<>("Error.TooManyBlocks", Parser.STRING_TO_STRING_PARSER, TOO_MANY.getPath()));
        set.add(new DedicatedNode<>("Error.NoSpeedSet", Parser.STRING_TO_STRING_PARSER, NO_SPEED_SET.getPath()));
        set.add(new DedicatedNode<>("Error.FailedToFindLicenceSign", Parser.STRING_TO_STRING_PARSER, FAILED_TO_FIND_LICENCE.getPath()));
        set.add(new DedicatedNode<>("Error.NoSpecialBlock", Parser.STRING_TO_STRING_PARSER, NO_SPECIAL_BLOCK_FOUND.getPath()));
        set.add(new DedicatedNode<>("Error.NoSpecialNamedBlock", Parser.STRING_TO_STRING_PARSER, NO_SPECIAL_NAMED_BLOCK_FOUND.getPath()));
        return set;
    }
}
