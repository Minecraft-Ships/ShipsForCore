package org.ships.config.messages.messages.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.permissions.vessel.CrewPermission;

import java.util.Collection;
import java.util.List;

public class InfoDefaultPermissionMessage implements Message<CrewPermission> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Permission", "Default"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component
                .text("Default Permission: ")
                .color(NamedTextColor.AQUA)
                .append(Component.text("%" + Message.CREW_ID.adapterText() + "%").color(NamedTextColor.GOLD));
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.CREW_PERMISSION);
    }

    @Override
    public Component processMessage(@NotNull Component text, CrewPermission obj) {
        var permissionAdapters = MessageAdapters.getAdaptersFor(AdapterCategories.CREW_PERMISSION).toList();
        for (MessageAdapter<CrewPermission> adapter : permissionAdapters) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }
}
