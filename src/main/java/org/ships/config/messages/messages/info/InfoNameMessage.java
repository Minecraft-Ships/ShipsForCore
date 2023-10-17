package org.ships.config.messages.messages.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InfoNameMessage implements Message<Vessel> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Name"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component
                .text("Name: ")
                .color(NamedTextColor.AQUA)
                .append(Component.text("%" + Message.VESSEL_NAME.adapterText() + "%").color(NamedTextColor.GOLD));
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(this.getExactAdapters());
    }

    @Override
    public Component processMessage(@NotNull Component text, Vessel obj) {
        for (MessageAdapter<Vessel> adapter : this.getExactAdapters()) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }

    public Set<MessageAdapter<Vessel>> getExactAdapters() {
        return Collections.singleton(Message.VESSEL_NAME);
    }

}
