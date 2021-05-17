package org.ships.config.messages.messages;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InfoDefaultPermissionMessage implements Message {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Permission", "Default"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("Default Permission: ").withColour(NamedTextColours.AQUA).append(AText.ofPlain("%" + Message.CREW_ID.adapterText() + "%").withColour(NamedTextColours.GOLD));
    }

    @Override
    public Set<MessageAdapter> getAdapters() {
        return new HashSet<>(Arrays.asList(Message.CREW_NAME, Message.CREW_ID));
    }
}
