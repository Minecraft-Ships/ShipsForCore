package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.stream.Collectors;

public class ErrorFailedInMovement implements Message<Vessel> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "UnknownErrorInMovement"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("A unknown error occurred when moving");
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        return Message.VESSEL_ADAPTERS.parallelStream().collect(Collectors.toSet());
    }

    @Override
    public AText process(@NotNull AText text, Vessel obj) {
        for (MessageAdapter<Vessel> adapter : Message.VESSEL_ADAPTERS) {
            text = adapter.process(obj, text);
        }
        return text;
    }
}
