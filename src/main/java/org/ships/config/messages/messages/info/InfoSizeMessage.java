package org.ships.config.messages.messages.info;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InfoSizeMessage implements Message<Vessel> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Size"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("Current Size: ").withColour(NamedTextColours.AQUA).append(AText.ofPlain("%" + Message.VESSEL_SIZE.adapterText() + "%").withColour(NamedTextColours.GOLD));
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(getExactAdapters());
    }

    private Set<MessageAdapter<Vessel>> getExactAdapters() {
        return Collections.singleton(Message.VESSEL_SIZE);
    }

    @Override
    public AText process(AText text, Vessel obj) {
        for (MessageAdapter<Vessel> adapter : getExactAdapters()) {
            text = adapter.process(text, obj);
        }
        return text;
    }
}
