package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Collections;
import java.util.Set;

public class ErrorInvalidShipTypeMessage implements Message<String> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Creation", "InvalidShipType"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("Invalid Shiptype of '" + Message.INVALID_NAME + "'");
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return Collections.singleton(Message.INVALID_NAME);
    }

    @Override
    public AText process(AText text, String obj) {
        return Message.INVALID_NAME.process(text, obj);
    }
}
