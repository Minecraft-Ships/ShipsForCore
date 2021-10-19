package org.ships.config.messages;

import org.array.utils.ArrayUtils;
import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.ships.config.Config;
import org.ships.config.messages.messages.bar.BlockFinderBarMessage;
import org.ships.config.messages.messages.error.*;
import org.ships.config.messages.messages.info.*;
import org.ships.config.node.DedicatedNode;
import org.ships.config.node.ObjectDedicatedNode;
import org.ships.plugin.ShipsPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AdventureMessageConfig implements Config.KnownNodes {

    public static final InfoNameMessage INFO_NAME = new InfoNameMessage();
    public static final InfoIdMessage INFO_ID = new InfoIdMessage();
    public static final InfoMaxSpeedMessage INFO_MAX_SPEED = new InfoMaxSpeedMessage();
    public static final InfoAltitudeSpeedMessage INFO_ALTITUDE_SPEED = new InfoAltitudeSpeedMessage();
    public static final InfoSizeMessage INFO_SIZE = new InfoSizeMessage();
    public static final InfoDefaultPermissionMessage INFO_DEFAULT_PERMISSION = new InfoDefaultPermissionMessage();
    public static final InfoVesselInfoMessage INFO_VESSEL_INFO = new InfoVesselInfoMessage();
    public static final InfoFlagMessage INFO_FLAG = new InfoFlagMessage();
    public static final InfoEntitiesListMessage INFO_ENTITIES_LIST = new InfoEntitiesListMessage();
    public static final InfoEntitiesLineMessage INFO_ENTITIES_LINE = new InfoEntitiesLineMessage();
    public static final ErrorOversizedMessage ERROR_OVERSIZED = new ErrorOversizedMessage();
    public static final ErrorUndersizedMessage ERROR_UNDERSIZED = new ErrorUndersizedMessage();
    public static final ErrorTooManyOfBlockMessage ERROR_TOO_MANY_OF_BLOCK = new ErrorTooManyOfBlockMessage();
    public static final ErrorAlreadyMovingMessage ERROR_ALREADY_MOVING = new ErrorAlreadyMovingMessage();
    public static final ErrorVesselStillLoadingMessage ERROR_VESSEL_STILL_LOADING = new ErrorVesselStillLoadingMessage();
    public static final ErrorPermissionMissMatchMessage ERROR_PERMISSION_MISS_MATCH = new ErrorPermissionMissMatchMessage();
    public static final ErrorInvalidShipTypeMessage ERROR_INVALID_SHIP_TYPE = new ErrorInvalidShipTypeMessage();
    public static final ErrorInvalidShipNameMessage ERROR_INVALID_SHIP_NAME = new ErrorInvalidShipNameMessage();
    public static final ErrorCannotCreateOntopMessage ERROR_CANNOT_CREATE_ONTOP = new ErrorCannotCreateOntopMessage();
    public static final ErrorShipsSignIsMoving ERROR_SHIPS_SIGN_IS_MOVING = new ErrorShipsSignIsMoving();
    public static final BlockFinderBarMessage BAR_BLOCK_FINDER_ON_FIND = new BlockFinderBarMessage();
    public static final ErrorBlockInWayMessage ERROR_BLOCK_IN_WAY = new ErrorBlockInWayMessage();

    private final ConfigurationStream.ConfigurationFile file;
    private final Set<Message<?>> messages = new HashSet<>();

    public AdventureMessageConfig() {
        messages.add(INFO_NAME);
        messages.add(INFO_ID);
        messages.add(INFO_MAX_SPEED);
        messages.add(INFO_ALTITUDE_SPEED);
        messages.add(INFO_SIZE);
        messages.add(INFO_DEFAULT_PERMISSION);
        messages.add(INFO_VESSEL_INFO);
        messages.add(INFO_FLAG);
        messages.add(INFO_ENTITIES_LIST);
        messages.add(INFO_ENTITIES_LINE);
        messages.add(ERROR_OVERSIZED);
        messages.add(ERROR_UNDERSIZED);
        messages.add(ERROR_TOO_MANY_OF_BLOCK);
        messages.add(ERROR_ALREADY_MOVING);
        messages.add(ERROR_VESSEL_STILL_LOADING);
        messages.add(ERROR_PERMISSION_MISS_MATCH);
        messages.add(ERROR_INVALID_SHIP_TYPE);
        messages.add(ERROR_INVALID_SHIP_NAME);
        messages.add(ERROR_CANNOT_CREATE_ONTOP);
        messages.add(ERROR_SHIPS_SIGN_IS_MOVING);
        File file = new File(ShipsPlugin.getPlugin().getConfigFolder(), "Configuration/Messages." + TranslateCore.getPlatform().getConfigFormat().getFileType()[0]);
        this.file = TranslateCore.createConfigurationFile(file, TranslateCore.getPlatform().getConfigFormat());
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
