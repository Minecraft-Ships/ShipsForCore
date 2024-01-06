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

import java.util.Collection;
import java.util.List;

public class InfoAltitudeSpeedMessage implements Message<Vessel> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Speed", "Altitude"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component
                .text("Max Altitude Speed: ")
                .color(NamedTextColor.AQUA)
                .append(Component.text("%" + Message.VESSEL_SPEED.adapterText() + "%").color(NamedTextColor.GOLD));
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL);
    }

    @Override
    public Component processMessage(@NotNull Component text, Vessel obj) {
        List<MessageAdapter<Vessel>> vesselCategory = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL).toList();
        for (MessageAdapter<Vessel> adapter : vesselCategory) {
            text = adapter.processMessage(obj);
        }
        return text;
    }
}
