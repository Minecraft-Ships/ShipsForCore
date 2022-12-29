package org.ships.config.messages.messages.info;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InfoMaxSpeedMessage implements Message<Vessel> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Speed", "Max"};
    }

    @Override
    public AText getDefault() {
        return AText
                .ofPlain("Max Speed: ")
                .withColour(NamedTextColours.AQUA)
                .append(AText
                                .ofPlain("%" + Message.VESSEL_SPEED.adapterText() + "%")
                                .withColour(NamedTextColours.GOLD));
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(this.getExactAdapters());
    }

    private Set<MessageAdapter<Vessel>> getExactAdapters() {
        return new HashSet<>(Collections.singleton(Message.VESSEL_SPEED));
    }

    @Override
    public AText process(@NotNull AText text, Vessel obj) {
        for (MessageAdapter<Vessel> adapter : this.getExactAdapters()) {
            text = adapter.process(obj, text);
        }
        return text;
    }
}
