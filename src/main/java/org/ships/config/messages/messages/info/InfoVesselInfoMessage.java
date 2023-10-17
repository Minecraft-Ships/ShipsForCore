package org.ships.config.messages.messages.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(this.getExactAdapters());
    }

    private Set<MessageAdapter<String>> getExactAdapters() {
        Set<MessageAdapter<String>> set = new HashSet<>();
        set.add(Message.VESSEL_INFO_VALUE);
        set.add(Message.VESSEL_INFO_KEY);
        return set;
    }

    @Override
    public Component processMessage(@NotNull Component text, Map.Entry<String, String> obj) {
        text = Message.VESSEL_INFO_VALUE.processMessage(obj.getKey(), text);
        text = Message.VESSEL_INFO_KEY.processMessage(obj.getValue(), text);
        return text;
    }


}
