package org.ships.config.parsers.identify;

import org.ships.permissions.vessel.CrewPermission;

public class StringToCrewPermission extends StringToIdentifiable<CrewPermission>{

    public StringToCrewPermission() {
        super(CrewPermission.class);
    }
}
