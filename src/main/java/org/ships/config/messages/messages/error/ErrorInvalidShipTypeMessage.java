package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.config.ConfigAdapter;

import java.util.HashSet;
import java.util.Set;

public class ErrorInvalidShipTypeMessage implements Message<String> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Creation", "InvalidShipType"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("Invalid Shiptype of '" + Message.INVALID_NAME.adapterTextFormat() + "'");
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        Set<MessageAdapter<?>> set = new HashSet<>(Message.CONFIG_ADAPTERS);
        set.add(Message.INVALID_NAME);
        return set;
    }

    @Override
    public AText process(AText text, String obj) {
        for (ConfigAdapter adapter : Message.CONFIG_ADAPTERS) {
            text = adapter.process(text);
        }
        return Message.INVALID_NAME.process(obj, text);
    }
}
