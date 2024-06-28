package org.ships.config.messages.messages.bar;

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
import java.util.stream.Collectors;

public class BlockFinderBarMessage implements Message<PositionableShipsStructure> {
    @Override
    public String[] getPath() {
        return new String[]{"Bar", "BlockFinder", "OnFind"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text(MessageAdapters.STRUCTURE_SIZE.adapterTextFormat() + " / "
                                      + MessageAdapters.CONFIG_TRACK_LIMIT.adapterTextFormat());
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL_STRUCTURE);
    }

    @Override
    public Component processMessage(@NotNull Component text, PositionableShipsStructure obj) {
        List<MessageAdapter<PositionableShipsStructure>> vesselAdapters = MessageAdapters
                .getAdaptersFor(AdapterCategories.VESSEL_STRUCTURE)
                .collect(Collectors.toList());
        for (MessageAdapter<PositionableShipsStructure> adapter : vesselAdapters) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }
}
