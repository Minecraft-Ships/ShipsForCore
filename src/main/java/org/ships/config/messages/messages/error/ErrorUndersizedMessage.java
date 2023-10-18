package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.config.ConfigAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ErrorUndersizedMessage implements Message<Map.Entry<Vessel, Integer>> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Undersized"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text(Message.VESSEL_ID.adapterTextFormat() + " is under the min size of "
                                      + Message.VESSEL_SIZE_ERROR.adapterTextFormat());
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        Set<MessageAdapter<?>> set = new HashSet<>();
        set.addAll(Message.VESSEL_ADAPTERS);
        set.addAll(Message.CONFIG_ADAPTERS);
        set.addAll(this.getErrorAdapters());
        return set;
    }

    private Set<MessageAdapter<Integer>> getErrorAdapters() {
        return Collections.singleton(Message.VESSEL_SIZE_ERROR);
    }

    @Override
    public Component processMessage(@NotNull Component text, Map.Entry<Vessel, Integer> obj) {
        for (ConfigAdapter<?> adapter : Message.CONFIG_ADAPTERS) {
            text = adapter.processMessage(text);
        }
        for (MessageAdapter<Vessel> adapter : Message.VESSEL_ADAPTERS) {
            text = adapter.processMessage(obj.getKey(), text);
        }
        for (MessageAdapter<Integer> adapter : this.getErrorAdapters()) {
            text = adapter.processMessage(obj.getValue(), text);
        }
        return text;
    }
}
