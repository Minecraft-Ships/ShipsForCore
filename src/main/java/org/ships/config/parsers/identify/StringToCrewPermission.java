package org.ships.config.parsers.identify;

import org.ships.permissions.vessel.CrewPermission;
import org.ships.permissions.vessel.CrewPermissions;

public class StringToCrewPermission extends StringToIdentifiable<CrewPermission> {

    public StringToCrewPermission() {
        super(() -> CrewPermissions.permissions().stream());
    }
}
