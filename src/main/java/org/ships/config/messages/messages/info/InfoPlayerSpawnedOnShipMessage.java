package org.ships.config.messages.messages.info;

import org.core.adventureText.AText;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.HashSet;

public class InfoPlayerSpawnedOnShipMessage implements Message<Vessel> {
    @Override
    public String[] getPath() {
        return new String[]{"info", "PlayerSpawnedOnShip"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("You have spawned on " + Message.VESSEL_NAME.adapterTextFormat());
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(Message.VESSEL_ADAPTERS);
    }

    @Override
    public AText process(@NotNull AText text, Vessel obj) {
        for (MessageAdapter<Vessel> adapter : Message.VESSEL_ADAPTERS) {
            if (adapter.containsAdapter(text)) {
                text = adapter.process(obj, text);
            }
        }
        return text;
    }
}
