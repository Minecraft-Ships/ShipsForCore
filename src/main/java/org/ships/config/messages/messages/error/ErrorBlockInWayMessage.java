package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.core.world.position.block.BlockType;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.sync.SyncPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.config.messages.adapter.misc.CollectionAdapter;
import org.ships.config.messages.adapter.misc.MappedAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.*;
import java.util.stream.Collectors;

public class ErrorBlockInWayMessage implements Message<Map.Entry<Vessel, Collection<BlockPosition>>> {

    private static final MappedAdapter<Position<?>, BlockType> BLOCK_TYPE_NAME = new MappedAdapter<>(BlockType.class,
                                                                                                     MessageAdapters.BLOCK_TYPE_NAME,
                                                                                                     Position::getBlockType);
    private static final MappedAdapter<Position<?>, BlockType> BLOCK_TYPE_ID = new MappedAdapter<>(BlockType.class,
                                                                                                   MessageAdapters.BLOCK_TYPE_ID,
                                                                                                   Position::getBlockType);

    @Override
    public String[] getPath() {
        return new String[]{"Error", "Block In Way"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text(
                MessageAdapters.VESSEL_ID.adapterTextFormat() + " cannot move due to " + MessageAdapters.BLOCK_NAMES.adapterTextFormat()
                        + " in way");
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL,
                       AdapterCategories.POSITION.<Collection<BlockPosition>>map(BlockPosition.class));
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        Set<MessageAdapter<?>> set = new HashSet<>(Message.super.getAdapters());
        set.add(BLOCK_TYPE_NAME);
        set.add(BLOCK_TYPE_ID);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public Component processMessage(@NotNull Component text, Map.Entry<Vessel, Collection<BlockPosition>> obj) {
        List<CollectionAdapter<SyncPosition<? extends Number>>> locationAdapters = MessageAdapters
                .getAdaptersFor(AdapterCategories.POSITION)
                .map(CollectionAdapter::new)
                .collect(Collectors.toList());
        List<MessageAdapter<Vessel>> vesselAdapters = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL).collect(
                Collectors.toList());

        List<? extends SyncPosition<? extends Number>> locations = obj
                .getValue()
                .stream()
                .map(pos -> (SyncPosition<? extends Number>) pos)
                .collect(Collectors.toList());

        for (MessageAdapter<Collection<SyncPosition<? extends Number>>> adapter : locationAdapters) {
            text = adapter.processMessage((Collection<SyncPosition<? extends Number>>) locations, text);
        }
        for (MessageAdapter<Vessel> adapter : vesselAdapters) {
            text = adapter.processMessage(obj.getKey(), text);
        }
        return text;
    }
}
