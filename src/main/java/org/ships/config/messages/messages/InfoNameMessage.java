package org.ships.config.messages.messages;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InfoNameMessage implements Message {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Name"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("Name: ").withColour(NamedTextColours.AQUA).append(AText.ofPlain("%" + Message.VESSEL_NAME.adapterText() + "%").withColour(NamedTextColours.GOLD));
    }

    @Override
    public Set<MessageAdapter> getAdapters() {
        return new HashSet<>(Collections.singleton(Message.VESSEL_NAME));
    }
}
