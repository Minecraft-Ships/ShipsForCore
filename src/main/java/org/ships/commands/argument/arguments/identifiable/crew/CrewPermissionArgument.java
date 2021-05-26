package org.ships.commands.argument.arguments.identifiable.crew;

import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.permissions.vessel.CrewPermission;

import java.util.function.Predicate;

public class CrewPermissionArgument extends ShipIdentifiableArgument<CrewPermission> {
    public CrewPermissionArgument(String id, Class<CrewPermission> type) {
        super(id, type);
    }

    public CrewPermissionArgument(String id, Class<CrewPermission> type, Predicate<CrewPermission> predicate) {
        super(id, type, predicate);
    }
}
