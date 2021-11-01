package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.config.ConfigAdapter;

import java.util.HashSet;
import java.util.Set;

public class ErrorShipsSignIsMoving implements Message<Object> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "ShipSignIsMoving"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("Ships sign is already moving ship").withColour(NamedTextColours.RED);
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(Message.CONFIG_ADAPTERS);
    }

    @Override
    @Deprecated
    public AText process(AText text, Object obj) {
        for (ConfigAdapter adapter : Message.CONFIG_ADAPTERS) {
            text = adapter.process(text);
        }
        return text;
    }
}
