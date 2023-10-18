package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Set;
import java.util.stream.Collectors;

public class ErrorNoSpeedSetMessage implements Message<BlockPosition> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "NoSpeedSet"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("Speed has not been set on sign");
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return Message.LOCATION_ADAPTERS.parallelStream().collect(Collectors.toSet());
    }

    @Override
    public Component processMessage(@NotNull Component text, BlockPosition obj) {
        for (MessageAdapter<?> adapter : this.getAdapters()) {
            if (adapter.containsAdapter(text)) {
                text = ((MessageAdapter<BlockPosition>) adapter).processMessage(obj, text);
            }
        }
        return text;
    }
}
