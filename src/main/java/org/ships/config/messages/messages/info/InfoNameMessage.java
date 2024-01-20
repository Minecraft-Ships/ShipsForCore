package org.ships.config.messages.messages.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.vessel.common.types.Vessel;

import java.util.*;
import java.util.stream.Collectors;

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
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL);
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(this.getExactAdapters());
    }

    @Override
    public Component processMessage(@NotNull Component text, Vessel obj) {
        var vesselAdapter = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL).collect(Collectors.toList());


        for (MessageAdapter<Vessel> adapter : vesselAdapter) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }

    @Deprecated(forRemoval = true)
    public Set<MessageAdapter<Vessel>> getExactAdapters() {
        return Collections.singleton(Message.VESSEL_NAME);
    }

}
