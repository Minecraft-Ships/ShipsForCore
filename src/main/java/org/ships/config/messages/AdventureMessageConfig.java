package org.ships.config.messages;

import net.kyori.adventure.text.Component;
import org.core.TranslateCore;
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

    public static final InfoPlayerSpawnedOnShipMessage INFO_PLAYER_SPAWNED_ON_SHIP = new InfoPlayerSpawnedOnShipMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorPreventMovementMessage ERROR_PREVENT_MOVEMENT = new ErrorPreventMovementMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorFailedInMovement ERROR_FAILED_IN_MOVEMENT = new ErrorFailedInMovement();
    @Deprecated(forRemoval = true)
    public static final InfoNameMessage INFO_NAME = new InfoNameMessage();
    @Deprecated(forRemoval = true)
    public static final InfoIdMessage INFO_ID = new InfoIdMessage();
    @Deprecated(forRemoval = true)
    public static final InfoMaxSpeedMessage INFO_MAX_SPEED = new InfoMaxSpeedMessage();
    @Deprecated(forRemoval = true)
    public static final InfoAltitudeSpeedMessage INFO_ALTITUDE_SPEED = new InfoAltitudeSpeedMessage();
    @Deprecated(forRemoval = true)
    public static final InfoSizeMessage INFO_SIZE = new InfoSizeMessage();
    @Deprecated(forRemoval = true)
    public static final InfoDefaultPermissionMessage INFO_DEFAULT_PERMISSION = new InfoDefaultPermissionMessage();
    @Deprecated(forRemoval = true)
    public static final InfoVesselInfoMessage INFO_VESSEL_INFO = new InfoVesselInfoMessage();
    @Deprecated(forRemoval = true)
    public static final InfoFlagMessage INFO_FLAG = new InfoFlagMessage();
    @Deprecated(forRemoval = true)
    public static final InfoEntitiesListMessage INFO_ENTITIES_LIST = new InfoEntitiesListMessage();
    @Deprecated(forRemoval = true)
    public static final InfoEntitiesLineMessage INFO_ENTITIES_LINE = new InfoEntitiesLineMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorOversizedMessage ERROR_OVERSIZED = new ErrorOversizedMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorUndersizedMessage ERROR_UNDERSIZED = new ErrorUndersizedMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorNotEnoughFuelMessage ERROR_NOT_ENOUGH_FUEL = new ErrorNotEnoughFuelMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorTooManyOfBlockMessage ERROR_TOO_MANY_OF_BLOCK = new ErrorTooManyOfBlockMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorAlreadyMovingMessage ERROR_ALREADY_MOVING = new ErrorAlreadyMovingMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorVesselStillLoadingMessage ERROR_VESSEL_STILL_LOADING = new ErrorVesselStillLoadingMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorPermissionMissMatchMessage ERROR_PERMISSION_MISS_MATCH = new ErrorPermissionMissMatchMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorInvalidShipTypeMessage ERROR_INVALID_SHIP_TYPE = new ErrorInvalidShipTypeMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorInvalidShipNameMessage ERROR_INVALID_SHIP_NAME = new ErrorInvalidShipNameMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorCannotCreateOntopMessage ERROR_CANNOT_CREATE_ONTOP = new ErrorCannotCreateOntopMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorShipsSignIsMoving ERROR_SHIPS_SIGN_IS_MOVING = new ErrorShipsSignIsMoving();
    @Deprecated(forRemoval = true)
    public static final BlockFinderBarMessage BAR_BLOCK_FINDER_ON_FIND = new BlockFinderBarMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorBlockInWayMessage ERROR_BLOCK_IN_WAY = new ErrorBlockInWayMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorSpecialBlockPercentNotEnough ERROR_SPECIAL_BLOCK_PERCENT_NOT_ENOUGH = new ErrorSpecialBlockPercentNotEnough();
    @Deprecated(forRemoval = true)
    public static final ErrorNoSpeedSetMessage ERROR_NO_SPEED_SET = new ErrorNoSpeedSetMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorNotMovingOnMessage ERROR_NOT_MOVING_ON = new ErrorNotMovingOnMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorCollideDetectedMessage ERROR_COLLIDE_DETECTED = new ErrorCollideDetectedMessage();
    @Deprecated(forRemoval = true)
    public static final ErrorFailedToFindLicenceSignMessage ERROR_FAILED_TO_FIND_LICENCE_SIGN = new ErrorFailedToFindLicenceSignMessage();

    @Deprecated(forRemoval = true)
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
        this.file = TranslateCore.getConfigManager().read(file, TranslateCore.getPlatform().getConfigFormat());
        this.recreateFile();
    }

    @Override
    public Set<DedicatedNode<Component, Component, ConfigurationNode.KnownParser.SingleKnown<Component>>> getNodes() {
        return this.messages
                .stream()
                .map(m -> new ObjectDedicatedNode<>(m.getConfigNode(), String.join(".", m.getPath())))
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
                .filter(m -> this.file.getString(m.getConfigNode()).isEmpty())
                .forEach(m -> this.file.set(m.getConfigNode(), m.getDefaultMessage()));
        this.file.save();
    }
}
