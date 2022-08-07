package org.ships.vessel.common.assits;

import org.core.TranslateCore;
import org.core.entity.living.human.player.User;
import org.core.utils.Else;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.vessel.common.types.Vessel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * specifies that the implementation of the vessel has crew with different roles
 */
public interface CrewStoredVessel extends Vessel {

    /**
     * Gets the crew of the ship
     *
     * @return a Map value containing the UUID of the player and their permission status
     */
    Map<UUID, CrewPermission> getCrew();

    /**
     * Gets the default crew permission for if the user who interacts with the ship is not specified within the map
     * value above.
     * This is not to be granted to the player in the map.
     *
     * @return the default permission
     */
    CrewPermission getDefaultPermission();

    /**
     * Gets the permission of the user. Note that it will default to the default permission if no user was found
     *
     * @param user The player in question
     * @return The permission the user has on this ship
     */
    default CrewPermission getPermission(UUID user) {
        CrewPermission permission = this.getCrew().get(user);
        if (permission == null) {
            permission = this.getDefaultPermission();
        }
        return permission;
    }

    /**
     * Gets all the users with a permission.
     *
     * @param permission The permission
     * @return The users with the specific permission
     */
    default Set<User> getUserCrew(CrewPermission permission) {
        return this.getCrew(permission)
                .stream()
                .map(uuid -> Else.throwOr(Exception.class, () -> TranslateCore.getServer().getOfflineUser(uuid).get(),
                        null))
                .filter(Objects::nonNull)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    /**
     * Gets all the users with a permission.
     *
     * @param permissionId The permission
     * @return The users with the specific permission
     */
    default Set<User> getUserCrew(String permissionId) {
        return this.getCrew(permissionId)
                .stream()
                .map(uuid -> Else.throwOr(Exception.class, () -> TranslateCore.getServer().getOfflineUser(uuid).get(),
                        null))
                .filter(Objects::nonNull)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    /**
     * Gets all the users uuids with permission
     *
     * @param permission The permission
     * @return The users uuids ith the specific permission
     */
    default Set<UUID> getCrew(CrewPermission permission) {
        Map<UUID, CrewPermission> permissionMap = this.getCrew();
        return permissionMap
                .keySet()
                .stream()
                .filter(u -> permissionMap.get(u).equals(permission))
                .collect(Collectors.toSet());
    }

    /**
     * Gets all the users uuids with permission
     *
     * @param permissionId The permission
     * @return The users uuids ith the specific permission
     */
    default Set<UUID> getCrew(String permissionId) {
        Map<UUID, CrewPermission> permissionMap = this.getCrew();
        return permissionMap
                .keySet()
                .stream()
                .filter(u -> permissionMap.get(u).getId().equals(permissionId))
                .collect(Collectors.toSet());
    }
}
