package org.ships.config.messages.messages.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.vessel.common.flag.VesselFlag;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class InfoFlagMessage implements Message<VesselFlag<?>> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Flag"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component
                .text("Flags: ")
                .color(NamedTextColor.AQUA)
                .append(Component.text(Message.VESSEL_FLAG_ID.adapterTextFormat()).color(NamedTextColor.GOLD));
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL_FLAG);
    }

    @Override
    public Component processMessage(@NotNull Component text, VesselFlag<?> obj) {
        List<MessageAdapter<VesselFlag<?>>> flagAdapters = MessageAdapters
                .getAdaptersFor(AdapterCategories.VESSEL_FLAG)
                .collect(Collectors.toList());
        for (MessageAdapter<VesselFlag<?>> adapter : flagAdapters) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }
}
