package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ErrorNoSpeedSetMessage implements Message<BlockPosition> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Sign", "No Speed Set"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("Speed has not been set on sign");
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return Collections.singletonList(AdapterCategories.POSITION);
    }

    @Override
    public Component processMessage(@NotNull Component text, BlockPosition obj) {
        List<MessageAdapter<SyncPosition<? extends Number>>> positionAdapters = MessageAdapters
                .getAdaptersFor(AdapterCategories.POSITION)
                .toList();
        for (MessageAdapter<SyncPosition<? extends Number>> adapter : positionAdapters) {
            text = adapter.processMessage(obj.toSyncPosition(), text);

        }
        return text;
    }
}
