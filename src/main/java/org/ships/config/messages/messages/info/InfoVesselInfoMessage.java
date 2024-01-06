package org.ships.config.messages.messages.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class InfoVesselInfoMessage implements Message<Map.Entry<String, String>> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Vessel", "Info"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component
                .text("%" + Message.VESSEL_INFO_KEY.adapterText() + "%: ")
                .color(NamedTextColor.AQUA)
                .append(Component.text("%" + Message.VESSEL_INFO_VALUE.adapterText() + "%").color(NamedTextColor.GOLD));
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL_INFO);
    }

    @Override
    public Component processMessage(@NotNull Component text, Map.Entry<String, String> obj) {
        var infoAdapters = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL_INFO).toList();
        for (var adapter : infoAdapters) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }


}
