package org.ships.config.messages.messages;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InfoVesselInfoMessage implements Message {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Vessel", "Info"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("%" + Message.VESSEL_INFO_KEY.adapterText() + "%: ").withColour(NamedTextColours.AQUA).append(AText.ofPlain("%" + Message.VESSEL_INFO_VALUE.adapterText() + "%").withColour(NamedTextColours.GOLD));
    }

    @Override
    public Set<MessageAdapter> getAdapters() {
        return new HashSet<>(Arrays.asList(Message.VESSEL_INFO_VALUE, Message.VESSEL_INFO_KEY));
    }
}
