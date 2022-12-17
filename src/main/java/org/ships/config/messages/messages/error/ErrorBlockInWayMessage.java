package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.misc.MappedAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ErrorBlockInWayMessage implements Message<Map.Entry<Vessel, Collection<BlockPosition>>> {

    @Override
    public String[] getPath() {
        return new String[]{"Error", "Block In Way"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain(Message.VESSEL_ID.adapterTextFormat() + " cannot move due to " + Message
                .asCollectionSingle(Message.LOCATION_ADAPTERS)
                .adapterTextFormat(new MappedAdapter<>(Message.BLOCK_TYPE_NAME, Position::getBlockType), 0)
                                     + " in way");
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        Set<MessageAdapter<?>> set = new HashSet<>();
        set.add(Message.asCollectionSingle(Message.LOCATION_ADAPTERS));
        set.addAll(Message.VESSEL_ADAPTERS);
        return set;
    }

    @Override
    public AText process(AText text, Map.Entry<Vessel, Collection<BlockPosition>> obj) {
        text = Message
                .asCollectionSingle(Message.LOCATION_ADAPTERS)
                .process(obj.getValue().parallelStream().collect(Collectors.toSet()), text);
        for (MessageAdapter<Vessel> ma : Message.VESSEL_ADAPTERS) {
            text = ma.process(obj.getKey(), text);
        }
        return text;
    }
}
