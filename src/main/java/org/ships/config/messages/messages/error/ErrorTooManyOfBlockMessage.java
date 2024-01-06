package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ErrorTooManyOfBlockMessage implements Message<Map.Entry<Vessel, BlockType>> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Requirement", "Special", "Too Many Of Blocks"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component
                .text("Too many of " + Message.BLOCK_TYPE_NAME.adapterTextFormat() + " found")
                .color(NamedTextColor.RED);
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL, AdapterCategories.BLOCK_TYPE);
    }

    @Override
    public Component processMessage(@NotNull Component text, Map.Entry<Vessel, BlockType> obj) {
        List<MessageAdapter<Vessel>> vesselAdapters = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL).toList();
        List<MessageAdapter<BlockType>> blockTypeAdapters = MessageAdapters
                .getAdaptersFor(AdapterCategories.BLOCK_TYPE)
                .toList();

        for (MessageAdapter<Vessel> adapter : vesselAdapters) {
            text = adapter.processMessage(obj.getKey(), text);
        }
        for (MessageAdapter<BlockType> adapter : blockTypeAdapters) {
            text = adapter.processMessage(obj.getValue(), text);
        }
        return text;
    }
}
