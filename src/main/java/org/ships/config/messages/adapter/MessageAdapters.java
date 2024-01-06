package org.ships.config.messages.adapter;

import org.jetbrains.annotations.UnmodifiableView;
import org.ships.config.messages.adapter.block.BlockTypeIdAdapter;
import org.ships.config.messages.adapter.block.BlockTypeNameAdapter;
import org.ships.config.messages.adapter.block.group.BlockGroupIdAdapter;
import org.ships.config.messages.adapter.block.group.BlockGroupNameAdapter;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.config.messages.adapter.config.TrackLimitAdapter;
import org.ships.config.messages.adapter.entity.EntityNameAdapter;
import org.ships.config.messages.adapter.entity.type.EntityTypeIdAdapter;
import org.ships.config.messages.adapter.entity.type.EntityTypeNameAdapter;
import org.ships.config.messages.adapter.item.ItemIdAdapter;
import org.ships.config.messages.adapter.item.ItemNameAdapter;
import org.ships.config.messages.adapter.misc.InvalidNameAdapter;
import org.ships.config.messages.adapter.permission.PermissionNodeAdapter;
import org.ships.config.messages.adapter.specific.NamedBlockNameAdapter;
import org.ships.config.messages.adapter.specific.number.NumberAdapter;
import org.ships.config.messages.adapter.structure.StructureChunkSizeAdapter;
import org.ships.config.messages.adapter.structure.StructureSizeAdapter;
import org.ships.config.messages.adapter.vessel.VesselIdAdapter;
import org.ships.config.messages.adapter.vessel.VesselNameAdapter;
import org.ships.config.messages.adapter.vessel.VesselSpeedAdapter;
import org.ships.config.messages.adapter.vessel.crew.CrewIdAdapter;
import org.ships.config.messages.adapter.vessel.crew.CrewNameAdapter;
import org.ships.config.messages.adapter.vessel.flag.VesselFlagIdAdapter;
import org.ships.config.messages.adapter.vessel.flag.VesselFlagNameAdapter;
import org.ships.config.messages.adapter.vessel.info.VesselInfoKeyAdapter;
import org.ships.config.messages.adapter.vessel.info.VesselInfoValueAdapter;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Stream;

public final class MessageAdapters {

    public static final VesselIdAdapter VESSEL_ID = new VesselIdAdapter();
    public static final VesselNameAdapter VESSEL_NAME = new VesselNameAdapter();
    public static final VesselFlagIdAdapter VESSEL_FLAG_ID = new VesselFlagIdAdapter();
    public static final VesselFlagNameAdapter VESSEL_FLAG_NAME = new VesselFlagNameAdapter();
    public static final VesselInfoKeyAdapter VESSEL_INFO_KEY = new VesselInfoKeyAdapter();
    public static final VesselInfoValueAdapter VESSEL_INFO_VALUE = new VesselInfoValueAdapter();
    public static final VesselSpeedAdapter VESSEL_SPEED = new VesselSpeedAdapter();
    public static final CrewIdAdapter CREW_ID = new CrewIdAdapter();
    public static final CrewNameAdapter CREW_NAME = new CrewNameAdapter();
    public static final EntityTypeIdAdapter ENTITY_TYPE_ID = new EntityTypeIdAdapter();
    public static final EntityTypeNameAdapter ENTITY_TYPE_NAME = new EntityTypeNameAdapter();
    public static final EntityNameAdapter ENTITY_NAME = new EntityNameAdapter();
    public static final BlockTypeNameAdapter BLOCK_TYPE_NAME = new BlockTypeNameAdapter();
    public static final BlockTypeIdAdapter BLOCK_TYPE_ID = new BlockTypeIdAdapter();
    public static final PermissionNodeAdapter PERMISSION_NODE = new PermissionNodeAdapter();
    public static final InvalidNameAdapter INVALID_NAME = new InvalidNameAdapter();
    public static final StructureChunkSizeAdapter STRUCTURE_CHUNK_SIZE = new StructureChunkSizeAdapter();
    public static final StructureSizeAdapter STRUCTURE_SIZE = new StructureSizeAdapter();
    public static final NamedBlockNameAdapter NAMED_BLOCK_NAME = new NamedBlockNameAdapter();
    public static final TrackLimitAdapter CONFIG_TRACK_LIMIT = new TrackLimitAdapter();
    public static final ItemNameAdapter ITEM_NAME = new ItemNameAdapter();
    public static final ItemIdAdapter ITEM_ID = new ItemIdAdapter();
    public static final NumberAdapter<Integer> FUEL_FOUND_REQUIREMENT = new NumberAdapter<>("Fuel found");
    public static final NumberAdapter<Integer> FUEL_CONSUMPTION_REQUIREMENT = new NumberAdapter<>("Fuel consumption");
    public static final NumberAdapter<Integer> FUEL_LEFT_REQUIREMENT = new NumberAdapter<>("Fuel left");
    public static final NumberAdapter<Integer> TOTAL_FOUND_BLOCKS = new NumberAdapter<>("total found blocks");
    public static final NumberAdapter<Double> PERCENT_FOUND = new NumberAdapter<>("Percent found");
    public static final BlockGroupIdAdapter BLOCK_IDS = new BlockGroupIdAdapter("block ids");
    public static final BlockGroupNameAdapter BLOCK_NAMES = new BlockGroupNameAdapter("block names");
    private static final Collection<MessageAdapter<?>> cached = new LinkedTransferQueue<>();

    private MessageAdapters() {
        throw new RuntimeException("Should not create");
    }

    public static <T> Stream<MessageAdapter<T>> getAdaptersFor(AdapterCategory<T> category) {
        return getAdapters().parallelStream().filter(category::canAccept).map(category::onAccept);
    }

    public static <T> Stream<MessageAdapter<T>> getAdaptersFor(Class<?> adaptingType) {
        return getAdapters()
                .parallelStream()
                .filter(adapter -> adaptingType.isAssignableFrom(adapter.adaptingType()))
                .map(adapter -> (MessageAdapter<T>) adapter);
    }

    @UnmodifiableView
    public static Collection<MessageAdapter<?>> getAdapters() {
        if (cached.isEmpty()) {
            List<? extends MessageAdapter<?>> streamed = Arrays
                    .stream(MessageAdapters.class.getDeclaredFields())
                    .filter(field -> Modifier.isPublic(field.getModifiers()))
                    .filter(field -> Modifier.isStatic(field.getModifiers()))
                    .filter(field -> Modifier.isFinal(field.getModifiers()))
                    .filter(field -> MessageAdapter.class.isAssignableFrom(field.getType()))
                    .map(field -> {
                        try {
                            return (MessageAdapter<?>) field.get(null);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
            cached.addAll(streamed);
        }
        return cached;
    }


}
