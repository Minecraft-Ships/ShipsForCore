package org.ships.config.messages.messages.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.permissions.vessel.CrewPermission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(this.getExactAdapters());
    }

    private Set<MessageAdapter<CrewPermission>> getExactAdapters() {
        return new HashSet<>(Arrays.asList(Message.CREW_NAME, Message.CREW_ID));
    }

    @Override
    public Component processMessage(@NotNull Component text, CrewPermission obj) {
        for (MessageAdapter<CrewPermission> adapter : this.getExactAdapters()) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }
}
