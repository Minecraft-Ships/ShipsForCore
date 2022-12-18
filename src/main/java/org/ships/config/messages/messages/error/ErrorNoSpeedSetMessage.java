package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.core.world.position.impl.BlockPosition;
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
    public AText getDefault() {
        return AText.ofPlain("Speed has not been set on sign");
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return Message.LOCATION_ADAPTERS.parallelStream().collect(Collectors.toSet());
    }

    @Override
    public AText process(AText text, BlockPosition obj) {
        for (MessageAdapter<?> adapter : this.getAdapters()) {
            if (adapter.containsAdapter(text)) {
                text = ((MessageAdapter<BlockPosition>) adapter).process(obj, text);
            }
        }
        return text;
    }
}