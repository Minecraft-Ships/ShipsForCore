package org.ships.config.messages.messages.info;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.common.flag.VesselFlag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InfoFlagMessage implements Message<VesselFlag<?>> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Flag"};
    }

    @Override
    public AText getDefault() {
        return AText
                .ofPlain("Flags: ")
                .withColour(NamedTextColours.AQUA)
                .append(AText.ofPlain(Message.VESSEL_FLAG_ID.adapterTextFormat()).withColour(NamedTextColours.GOLD));
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(this.getExactAdapters());
    }


    private Set<MessageAdapter<VesselFlag<?>>> getExactAdapters() {
        return new HashSet<>(Arrays.asList(Message.VESSEL_FLAG_ID, Message.VESSEL_FLAG_NAME));
    }

    @Override
    public AText process(AText text, VesselFlag<?> obj) {
        for (MessageAdapter<VesselFlag<?>> adapter : this.getExactAdapters()) {
            text = adapter.process(obj, text);
        }
        return text;
    }
}
