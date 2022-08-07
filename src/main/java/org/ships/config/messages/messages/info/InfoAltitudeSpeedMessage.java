package org.ships.config.messages.messages.info;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.config.ConfigAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.HashSet;
import java.util.Set;

public class InfoAltitudeSpeedMessage implements Message<Vessel> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Speed", "Altitude"};
    }

    @Override
    public AText getDefault() {
        return AText
                .ofPlain("Max Altitude Speed: ")
                .withColour(NamedTextColours.AQUA)
                .append(AText
                        .ofPlain("%" + Message.VESSEL_SPEED.adapterText() + "%")
                        .withColour(NamedTextColours.GOLD));
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        Set<MessageAdapter<?>> set = new HashSet<>(Message.CONFIG_ADAPTERS);
        set.add(Message.VESSEL_SPEED);
        return set;
    }

    @Override
    public AText process(AText text, Vessel obj) {
        for (ConfigAdapter adapter : Message.CONFIG_ADAPTERS) {
            text = adapter.process(text);
        }
        return Message.VESSEL_SIZE.process(text, obj);
    }
}
