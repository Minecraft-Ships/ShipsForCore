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
import org.ships.config.messages.adapter.misc.CollectionAdapter;
import org.ships.config.messages.messages.error.data.CollideDetectedMessageData;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.List;

public class ErrorCollideDetectedMessage implements Message<CollideDetectedMessageData> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Collide Detected"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("Found blocks in the way");
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL,
                       AdapterCategories.POSITION.<Collection<BlockPosition>>map(Collection.class));
    }


    @Override
    public Component processMessage(@NotNull Component text, CollideDetectedMessageData obj) {
        List<CollectionAdapter<SyncPosition<? extends Number>>> positionAdapters = MessageAdapters
                .getAdaptersFor(AdapterCategories.POSITION)
                .map(CollectionAdapter::new)
                .toList();
        List<MessageAdapter<Vessel>> vesselAdapters = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL).toList();

        var positions = obj
                .getPositions()
                .stream()
                .map(BlockPosition::toSyncPosition)
                .map(pos -> (SyncPosition<? extends Number>) pos)
                .toList();

        for (MessageAdapter<Vessel> adapter : vesselAdapters) {
            text = adapter.processMessage(obj.getVessel(), text);
        }
        for (CollectionAdapter<SyncPosition<? extends Number>> adapter : positionAdapters) {
            text = adapter.processMessage((Collection<SyncPosition<? extends Number>>) (Object) positions, text);
        }
        return text;
    }
}
