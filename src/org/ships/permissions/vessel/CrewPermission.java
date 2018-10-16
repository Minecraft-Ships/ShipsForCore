package org.ships.permissions.vessel;

import org.core.utils.Identifable;

public interface CrewPermission extends Identifable {

    boolean canInteract();
    boolean canMove();
    boolean canCommand();
}
