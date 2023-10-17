package org.ships.config.messages.messages.info;

import net.kyori.adventure.text.Component;
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
    public Component getDefaultMessage() {
        return Component.text("You have spawned on " + Message.VESSEL_NAME.adapterTextFormat());
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(Message.VESSEL_ADAPTERS);
    }

    @Override
    public Component processMessage(@NotNull Component text, Vessel obj) {
        for (MessageAdapter<Vessel> adapter : Message.VESSEL_ADAPTERS) {
            if (adapter.containsAdapter(text)) {
                text = adapter.processMessage(obj, text);
            }
        }
        return text;
    }
}
