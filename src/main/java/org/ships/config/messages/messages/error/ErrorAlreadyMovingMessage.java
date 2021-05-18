package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.HashSet;
import java.util.Set;

public class ErrorAlreadyMovingMessage implements Message<Vessel> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "AlreadyMoving"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain(Message.VESSEL_ID.adapterTextFormat() + " is already moving. Please wait for it to finish");
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(Message.VESSEL_ADAPTERS);
    }

    @Override
    public AText process(AText text, Vessel obj) {
        for (MessageAdapter<Vessel> adapter : Message.VESSEL_ADAPTERS) {
            text = adapter.process(text, obj);
        }
        return text;
    }
}
