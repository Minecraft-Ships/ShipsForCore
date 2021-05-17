package org.ships.config.messages.messages;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.HashSet;
import java.util.Set;

public class InfoIdMessage implements Message {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Id"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("Id: ").withColour(NamedTextColours.AQUA);
    }

    @Override
    public Set<MessageAdapter> getAdapters() {
        return new HashSet<>();
    }
}
