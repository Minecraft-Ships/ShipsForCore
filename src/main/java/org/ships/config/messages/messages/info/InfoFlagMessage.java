package org.ships.config.messages.messages.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.common.flag.VesselFlag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(this.getExactAdapters());
    }


    private Set<MessageAdapter<VesselFlag<?>>> getExactAdapters() {
        return new HashSet<>(Arrays.asList(Message.VESSEL_FLAG_ID, Message.VESSEL_FLAG_NAME));
    }

    @Override
    public Component processMessage(@NotNull Component text, VesselFlag<?> obj) {
        for (MessageAdapter<VesselFlag<?>> adapter : this.getExactAdapters()) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }
}
