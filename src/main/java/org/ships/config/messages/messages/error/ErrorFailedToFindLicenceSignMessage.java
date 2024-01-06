package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Collection;
import java.util.List;

public class ErrorFailedToFindLicenceSignMessage implements Message<PositionableShipsStructure> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Sign", "Failed To Find"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("Failed to find licence sign");
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL_STRUCTURE);
    }

    @Override
    public Component processMessage(@NotNull Component text, PositionableShipsStructure obj) {
        List<MessageAdapter<PositionableShipsStructure>> structureAdapters = MessageAdapters
                .getAdaptersFor(AdapterCategories.VESSEL_STRUCTURE)
                .toList();
        for (MessageAdapter<PositionableShipsStructure> adapter : structureAdapters) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }
}
