package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.messages.error.data.NotMovingOnMessageData;

import java.util.Collection;
import java.util.HashSet;

public class ErrorNotMovingOnMessage implements Message<NotMovingOnMessageData> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "NotInMovingIn"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("Cannot move. Needs to move onto either %Block Names%");
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        Collection<MessageAdapter<?>> collection = new HashSet<>();
        collection.addAll(Message.VESSEL_ADAPTERS);

        return collection;
    }

    @Override
    public AText process(AText text, NotMovingOnMessageData obj) {
        return null;
    }
}
