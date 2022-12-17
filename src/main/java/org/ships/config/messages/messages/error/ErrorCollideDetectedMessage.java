package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.core.world.position.impl.Position;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.misc.CollectionAdapter;
import org.ships.config.messages.messages.error.data.CollideDetectedMessageData;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class ErrorCollideDetectedMessage implements Message<CollideDetectedMessageData> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "CollideDetected"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("Found blocks in the way");
    }

    private Collection<CollectionAdapter<Position<?>>> getPositionAdapters() {
        return Message.LOCATION_ADAPTERS.parallelStream().map(CollectionAdapter::new).collect(Collectors.toSet());
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        Collection<MessageAdapter<?>> collection = new LinkedList<>();
        collection.addAll(Message.VESSEL_ADAPTERS);
        collection.addAll(this.getPositionAdapters());
        return collection;
    }

    @Override
    public AText process(@NotNull AText text, CollideDetectedMessageData obj) {
        for (MessageAdapter<Vessel> adapter : Message.VESSEL_ADAPTERS) {
            if (adapter.containsAdapter(text)) {
                text = adapter.process(obj.getVessel(), text);
            }
        }
        for (CollectionAdapter<Position<?>> adapter : this.getPositionAdapters()) {
            if (adapter.containsAdapter(text)) {
                text = adapter.process(obj.getPositions().parallelStream().collect(Collectors.toSet()), text);
            }
        }
        return text;
    }
}
