package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.config.ConfigAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ErrorOversizedMessage implements Message<Map.Entry<Vessel, Integer>> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Oversized"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain(Message.VESSEL_ID.adapterTextFormat() + " is over the max size of "
                                     + Message.VESSEL_SIZE_ERROR.adapterTextFormat());
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        Set<MessageAdapter<?>> set = new HashSet<>();
        set.add(Message.VESSEL_SIZE_ERROR);
        set.addAll(Message.VESSEL_ADAPTERS);
        set.addAll(Message.CONFIG_ADAPTERS);
        return set;
    }

    @Override
    public AText process(@NotNull AText text, Map.Entry<Vessel, Integer> obj) {
        for (ConfigAdapter adapter : Message.CONFIG_ADAPTERS) {
            text = adapter.process(text);
        }
        for (MessageAdapter<Vessel> adapter : Message.VESSEL_ADAPTERS) {
            text = adapter.process(obj.getKey(), text);
        }
        return Message.VESSEL_SIZE_ERROR.process(obj.getValue(), text);

    }
}
