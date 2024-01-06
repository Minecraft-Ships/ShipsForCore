package org.ships.config.messages.adapter.permission;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.permissions.Permissions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PermissionNodeAdapter implements MessageAdapter<String> {
    @Override
    public String adapterText() {
        return "Permission Node";
    }

    @Override
    public Class<?> adaptingType() {
        return String.class;
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(Permissions.CMD_INFO.getPermissionValue());
    }

    @Override
    public Collection<AdapterCategory<String>> categories() {
        return List.of(AdapterCategories.PERMISSION);
    }

    @Override
    public Component processMessage(@NotNull String obj) {
        return Component.text(obj);
    }
}
