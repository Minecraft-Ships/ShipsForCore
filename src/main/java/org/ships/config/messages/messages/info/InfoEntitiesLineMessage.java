package org.ships.config.messages.messages.info;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Collections;
import java.util.Set;

public class InfoEntitiesLineMessage implements Message<Object> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Entities", "Line"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("Entities:").withColour(NamedTextColours.AQUA);
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return Collections.emptySet();
    }

    @Override
    @Deprecated
    public AText process(AText text, Object obj) {
        return text;
    }
}
