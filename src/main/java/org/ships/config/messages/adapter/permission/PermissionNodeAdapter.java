package org.ships.config.messages.adapter.permission;

import org.core.adventureText.AText;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.permissions.Permissions;

import java.util.Collections;
import java.util.Set;

public class PermissionNodeAdapter implements MessageAdapter<String> {
    @Override
    public String adapterText() {
        return "Permission Node";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(Permissions.CMD_INFO.getPermissionValue());
    }

    @Override
    public AText process(@NotNull String obj) {
        return AText.ofPlain(obj);
    }
}
