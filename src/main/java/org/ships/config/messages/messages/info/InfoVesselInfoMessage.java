package org.ships.config.messages.messages.info;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InfoVesselInfoMessage implements Message<Map.Entry<String, String>> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Vessel", "Info"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("%" + Message.VESSEL_INFO_KEY.adapterText() + "%: ").withColour(NamedTextColours.AQUA).append(AText.ofPlain("%" + Message.VESSEL_INFO_VALUE.adapterText() + "%").withColour(NamedTextColours.GOLD));
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(getExactAdapters());
    }

    private Set<MessageAdapter<String>> getExactAdapters() {
        Set<MessageAdapter<String>> set = new HashSet<>();
        set.add(Message.VESSEL_INFO_VALUE);
        set.add(Message.VESSEL_INFO_KEY);
        return set;
    }

    @Override
    public AText process(AText text, Map.Entry<String, String> obj) {
        text = Message.VESSEL_INFO_VALUE.process(text, obj.getKey());
        text = Message.VESSEL_INFO_KEY.process(text, obj.getValue());
        return text;
    }


}
