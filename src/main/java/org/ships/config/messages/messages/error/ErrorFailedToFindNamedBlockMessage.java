package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.core.adventureText.AText;
import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.messages.error.data.NamedBlockMessageData;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.HashSet;

public class ErrorFailedToFindNamedBlockMessage implements Message<NamedBlockMessageData> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "NoSpecialNamedBlockFound"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("Failed to find %Block Name%");
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        Collection<MessageAdapter<?>> adapters = new HashSet<>();
        adapters.addAll(Message.VESSEL_ADAPTERS);
        adapters.addAll(Message.BLOCK_TYPE_ADAPTERS);
        adapters.add(Message.NAMED_BLOCK_NAME);
        return adapters;
    }

    @Override
    public Component processMessage(@NotNull Component text, NamedBlockMessageData obj) {
        if (Message.NAMED_BLOCK_NAME.containsAdapter(text)) {
            text = Message.NAMED_BLOCK_NAME.processMessage(obj.getNamedBlock(), text);
        }
        for (MessageAdapter<Vessel> adapters : Message.VESSEL_ADAPTERS) {
            if (adapters.containsAdapter(text)) {
                text = adapters.processMessage(obj.getVessel(), text);
            }
        }
        for (MessageAdapter<BlockType> adapters : Message.BLOCK_TYPE_ADAPTERS) {
            if (adapters.containsAdapter(text)) {
                text = adapters.processMessage(obj.getType(), text);
            }
        }
        return text;
    }
}
