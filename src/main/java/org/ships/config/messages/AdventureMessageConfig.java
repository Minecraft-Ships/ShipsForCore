package org.ships.config.messages;

import org.array.utils.ArrayUtils;
import org.core.CorePlugin;
import org.core.adventureText.AText;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.ships.config.Config;
import org.ships.config.messages.messages.*;
import org.ships.config.node.DedicatedNode;
import org.ships.config.node.ObjectDedicatedNode;
import org.ships.plugin.ShipsPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AdventureMessageConfig implements Config.KnownNodes {

    private final ConfigurationStream.ConfigurationFile file;
    private final Set<Message> messages = new HashSet<>();

    public static final InfoNameMessage INFO_NAME = new InfoNameMessage();
    public static final InfoIdMessage INFO_ID = new InfoIdMessage();
    public static final InfoMaxSpeedMessage INFO_MAX_SPEED = new InfoMaxSpeedMessage();
    public static final InfoAltitudeSpeedMessage INFO_ALTITUDE_SPEED = new InfoAltitudeSpeedMessage();
    public static final InfoSizeMessage INFO_SIZE = new InfoSizeMessage();
    public static final InfoDefaultPermissionMessage INFO_DEFAULT_PERMISSION = new InfoDefaultPermissionMessage();
    public static final InfoVesselInfoMessage INFO_VESSEL_INFO = new InfoVesselInfoMessage();

    public AdventureMessageConfig() {
        messages.add(INFO_NAME);
        messages.add(INFO_ID);
        messages.add(INFO_MAX_SPEED);
        messages.add(INFO_ALTITUDE_SPEED);
        messages.add(INFO_SIZE);
        messages.add(INFO_DEFAULT_PERMISSION);
        messages.add(INFO_VESSEL_INFO);
        File file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "Configuration/Messages." + CorePlugin.getPlatform().getConfigFormat().getFileType()[0]);
        this.file = CorePlugin.createConfigurationFile(file, CorePlugin.getPlatform().getConfigFormat());
        recreateFile();
    }

    @Override
    public Set<DedicatedNode<AText, AText, ConfigurationNode.KnownParser.SingleKnown<AText>>> getNodes() {
        return this.messages.stream().map(m -> new ObjectDedicatedNode<>(m.getKnownPath(), ArrayUtils.toString(".", t -> t, m.getPath()))).collect(Collectors.toSet());
    }

    @Override
    public ConfigurationStream.ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public void recreateFile() {
        this.messages
                .stream()
                .filter(m -> !this.file.getString(m.getKnownPath()).isPresent())
                .forEach(m -> this.file.set(m.getKnownPath(), m.getDefault()));
        this.file.save();
    }
}
