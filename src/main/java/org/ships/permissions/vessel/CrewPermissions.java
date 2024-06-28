package org.ships.permissions.vessel;

import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.LinkedTransferQueue;

public final class CrewPermissions {

    private static final Collection<CrewPermission> registered = new LinkedTransferQueue<>();

    public static final CrewPermission CAPTAIN = register(
            new AbstractCrewPermission("ships.captain", "Captain").setCanMove(true).setCommand(true).setRemove(true));

    public static final CrewPermission CREW_MEMBER = register(
            new AbstractCrewPermission("ships.member", "Crew").setCanMove(true).setCommand(false).setRemove(false));

    public static final CrewPermission DEFAULT = register(new AbstractCrewPermission("ships.default", "Default")
                                                                  .setCanMove(false)
                                                                  .setCommand(false)
                                                                  .setRemove(false));

    private CrewPermissions() {
    }

    public static CrewPermission register(CrewPermission permission) {
        registered.add(permission);
        return permission;
    }

    @UnmodifiableView
    public static Collection<CrewPermission> permissions() {
        return Collections.unmodifiableCollection(registered);
    }

}
