package org.ships.config.messages;

import org.jetbrains.annotations.UnmodifiableView;
import org.ships.config.messages.messages.bar.BlockFinderBarMessage;
import org.ships.config.messages.messages.error.*;
import org.ships.config.messages.messages.info.*;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.LinkedTransferQueue;

public final class Messages {

    public static final InfoPlayerSpawnedOnShipMessage INFO_PLAYER_SPAWNED_ON_SHIP = new InfoPlayerSpawnedOnShipMessage();
    public static final ErrorPreventMovementMessage ERROR_PREVENT_MOVEMENT = new ErrorPreventMovementMessage();
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

    private static final Collection<Message<?>> cached = new LinkedTransferQueue<>();

    private Messages() {
        throw new RuntimeException("Should not create");
    }

    @UnmodifiableView
    public static Collection<Message<?>> getMessages() {
        if (cached.isEmpty()) {
            cached.addAll(Arrays
                                  .stream(Messages.class.getDeclaredFields())
                                  .filter(field -> Modifier.isPublic(field.getModifiers()))
                                  .filter(field -> Modifier.isStatic(field.getModifiers()))
                                  .filter(field -> Modifier.isFinal(field.getModifiers()))
                                  .filter(field -> Message.class.isAssignableFrom(field.getType()))
                                  .map(field -> {
                                      try {
                                          return (Message<?>) field.get(null);
                                      } catch (IllegalAccessException e) {
                                          e.printStackTrace();
                                          return null;
                                      }
                                  })
                                  .filter(Objects::nonNull)
                                  .toList());
        }
        return cached;
    }
}
