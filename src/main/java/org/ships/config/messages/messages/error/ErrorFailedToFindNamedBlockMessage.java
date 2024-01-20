package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.config.messages.messages.error.data.NamedBlockMessageData;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ErrorFailedToFindNamedBlockMessage implements Message<NamedBlockMessageData> {

    @Override
    public String[] getPath() {
        return new String[]{"Error", "Special", "Block", "NoNamedBlockFound"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("Failed to find %Block Name%");
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL, AdapterCategories.BLOCK_TYPE);
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        Collection<MessageAdapter<?>> adapters = new HashSet<>(Message.super.getAdapters());
        adapters.add(Message.NAMED_BLOCK_NAME);
        return adapters;
    }

    @Override
    public Component processMessage(@NotNull Component text, NamedBlockMessageData obj) {
        List<MessageAdapter<Vessel>> vesselAdapters = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL).collect(
                Collectors.toList());
        List<MessageAdapter<BlockType>> blockTypeAdapters = MessageAdapters
                .getAdaptersFor(AdapterCategories.BLOCK_TYPE)
                .collect(Collectors.toList());

        for (MessageAdapter<Vessel> adapter : vesselAdapters) {
            text = adapter.processMessage(obj.getVessel(), text);
        }
        for (MessageAdapter<BlockType> adapter : blockTypeAdapters) {
            text = adapter.processMessage(obj.getType(), text);
        }
        text = Message.NAMED_BLOCK_NAME.processMessage(obj.getNamedBlock(), text);
        return text;
    }
}
