package org.ships.config.messages.messages.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.config.ConfigAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.HashSet;
import java.util.Set;

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
    public Set<MessageAdapter<?>> getAdapters() {
        Set<MessageAdapter<?>> set = new HashSet<>(Message.CONFIG_ADAPTERS);
        set.add(Message.VESSEL_SPEED);
        return set;
    }

    @Override
    public Component processMessage(@NotNull Component text, Vessel obj) {
        for (ConfigAdapter<?> adapter : Message.CONFIG_ADAPTERS) {
            text = adapter.processMessage(text);
        }
        return Message.VESSEL_SIZE.processMessage(obj, text);
    }
}
