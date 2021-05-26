package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.config.ConfigAdapter;

import java.util.HashSet;
import java.util.Set;

public class ErrorInvalidShipNameMessage implements Message<String> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Creation", "InvalidShipName"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("The name of '" + Message.INVALID_NAME + "' has already been taken");
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        Set<MessageAdapter<?>> set = new HashSet<>();
        set.add(Message.INVALID_NAME);
        set.addAll(Message.CONFIG_ADAPTERS);
        return set;
    }

    @Override
    public AText process(AText text, String obj) {
        for (ConfigAdapter adapter : Message.CONFIG_ADAPTERS) {
            text = adapter.process(text);
        }
        return Message.INVALID_NAME.process(text, obj);
    }
}
