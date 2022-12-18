package org.ships.config.messages;

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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AdventureMessageConfig implements Config.KnownNodes {

    public static final ErrorFailedInMovement ERROR_FAILED_IN_MOVEMENT = new ErrorFailedInMovement();
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
    public static final ErrorNotEnoughFuelMessage ERROR_NOT_ENOUGH_FUEL = new ErrorNotEnoughFuelMessage();
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
    public static final ErrorSpecialBlockPercentNotEnough ERROR_SPECIAL_BLOCK_PERCENT_NOT_ENOUGH = new ErrorSpecialBlockPercentNotEnough();

    public static final ErrorNoSpeedSetMessage ERROR_NO_SPEED_SET = new ErrorNoSpeedSetMessage();
    public static final ErrorNotMovingOnMessage ERROR_NOT_MOVING_ON = new ErrorNotMovingOnMessage();
    public static final ErrorCollideDetectedMessage ERROR_COLLIDE_DETECTED = new ErrorCollideDetectedMessage();
    public static final ErrorFailedToFindLicenceSignMessage ERROR_FAILED_TO_FIND_LICENCE_SIGN = new ErrorFailedToFindLicenceSignMessage();

    public static final ErrorFailedToFindNamedBlockMessage ERROR_FAILED_TO_FIND_NAMED_BLOCK = new ErrorFailedToFindNamedBlockMessage();
    private final ConfigurationStream.ConfigurationFile file;
    private final Collection<Message<?>> messages = new HashSet<>();

    public AdventureMessageConfig() {
        this.messages.add(INFO_NAME);
        this.messages.add(INFO_ID);
        this.messages.add(INFO_MAX_SPEED);
        this.messages.add(INFO_ALTITUDE_SPEED);
        this.messages.add(INFO_SIZE);
        this.messages.add(INFO_DEFAULT_PERMISSION);
        this.messages.add(INFO_VESSEL_INFO);
        this.messages.add(INFO_FLAG);
        this.messages.add(INFO_ENTITIES_LIST);
        this.messages.add(INFO_ENTITIES_LINE);
        this.messages.add(ERROR_OVERSIZED);
        this.messages.add(ERROR_UNDERSIZED);
        this.messages.add(ERROR_TOO_MANY_OF_BLOCK);
        this.messages.add(ERROR_ALREADY_MOVING);
        this.messages.add(ERROR_VESSEL_STILL_LOADING);
        this.messages.add(ERROR_PERMISSION_MISS_MATCH);
        this.messages.add(ERROR_INVALID_SHIP_TYPE);
        this.messages.add(ERROR_INVALID_SHIP_NAME);
        this.messages.add(ERROR_CANNOT_CREATE_ONTOP);
        this.messages.add(ERROR_SHIPS_SIGN_IS_MOVING);
        this.messages.add(ERROR_BLOCK_IN_WAY);
        this.messages.add(ERROR_NO_SPEED_SET);
        this.messages.add(ERROR_FAILED_TO_FIND_NAMED_BLOCK);
        this.messages.add(ERROR_NOT_MOVING_ON);
        this.messages.add(ERROR_SPECIAL_BLOCK_PERCENT_NOT_ENOUGH);
        File file = new File(ShipsPlugin.getPlugin().getConfigFolder(), "Configuration/Messages." + TranslateCore
                .getPlatform()
                .getConfigFormat()
                .getFileType()[0]);
        this.file = TranslateCore.createConfigurationFile(file, TranslateCore.getPlatform().getConfigFormat());
        this.recreateFile();
    }

    @Override
    public Set<DedicatedNode<AText, AText, ConfigurationNode.KnownParser.SingleKnown<AText>>> getNodes() {
        return this.messages
                .stream()
                .map(m -> new ObjectDedicatedNode<>(m.getKnownPath(), String.join(".", m.getPath())))
                .collect(Collectors.toSet());
    }

    @Override
    public ConfigurationStream.ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public void recreateFile() {
        this.messages
                .stream()
                .filter(m -> this.file.getString(m.getKnownPath()).isEmpty())
                .forEach(m -> this.file.set(m.getKnownPath(), m.getDefault()));
        this.file.save();
    }
}
