package org.ships.config.messages.messages.info;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class InfoPlayerSpawnedOnShipMessage implements Message<Vessel> {
    @Override
    public String[] getPath() {
        return new String[]{"info", "Player Spawned On Ship"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("You have spawned on " + Message.VESSEL_NAME.adapterTextFormat());
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return Collections.singletonList(AdapterCategories.VESSEL);
    }

    @Override
    public Component processMessage(@NotNull Component text, Vessel obj) {
        List<MessageAdapter<Vessel>> adapters = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL).toList();
        for (MessageAdapter<Vessel> adapter : adapters) {
            if (adapter.containsAdapter(text)) {
                text = adapter.processMessage(obj, text);
            }
        }

        return text;
    }
}
