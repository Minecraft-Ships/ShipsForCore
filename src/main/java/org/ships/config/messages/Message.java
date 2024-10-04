package org.ships.config.messages;

import net.kyori.adventure.text.Component;
import org.core.config.ConfigurationNode;
import org.core.config.parser.StringParser;
import org.core.entity.Entity;
import org.core.entity.EntityType;
import org.core.inventory.item.ItemType;
import org.core.world.position.block.BlockType;
import org.core.world.position.impl.Position;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.block.BlockTypeIdAdapter;
import org.ships.config.messages.adapter.block.BlockTypeNameAdapter;
import org.ships.config.messages.adapter.block.group.BlockGroupIdAdapter;
import org.ships.config.messages.adapter.block.group.BlockGroupNameAdapter;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.config.messages.adapter.config.ConfigAdapter;
import org.ships.config.messages.adapter.config.TrackLimitAdapter;
import org.ships.config.messages.adapter.entity.EntityNameAdapter;
import org.ships.config.messages.adapter.entity.type.EntityTypeIdAdapter;
import org.ships.config.messages.adapter.entity.type.EntityTypeNameAdapter;
import org.ships.config.messages.adapter.item.ItemIdAdapter;
import org.ships.config.messages.adapter.item.ItemNameAdapter;
import org.ships.config.messages.adapter.misc.CollectionSingleAdapter;
import org.ships.config.messages.adapter.misc.InvalidNameAdapter;
import org.ships.config.messages.adapter.misc.MappedAdapter;
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
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Collectors;

public interface Message<R> {
    @Deprecated(forRemoval = true)
    VesselIdAdapter VESSEL_ID = new VesselIdAdapter();
    @Deprecated(forRemoval = true)
    VesselNameAdapter VESSEL_NAME = new VesselNameAdapter();
    @Deprecated(forRemoval = true)
    VesselFlagIdAdapter VESSEL_FLAG_ID = new VesselFlagIdAdapter();
    @Deprecated(forRemoval = true)
    VesselFlagNameAdapter VESSEL_FLAG_NAME = new VesselFlagNameAdapter();
    @Deprecated(forRemoval = true)
    VesselInfoKeyAdapter VESSEL_INFO_KEY = new VesselInfoKeyAdapter();
    @Deprecated(forRemoval = true)
    VesselInfoValueAdapter VESSEL_INFO_VALUE = new VesselInfoValueAdapter();
    @Deprecated(forRemoval = true)
    VesselSpeedAdapter VESSEL_SPEED = new VesselSpeedAdapter();
    @Deprecated(forRemoval = true)
    CrewIdAdapter CREW_ID = new CrewIdAdapter();
    @Deprecated(forRemoval = true)
    CrewNameAdapter CREW_NAME = new CrewNameAdapter();
    @Deprecated(forRemoval = true)
    EntityTypeIdAdapter ENTITY_TYPE_ID = new EntityTypeIdAdapter();
    @Deprecated(forRemoval = true)
    EntityTypeNameAdapter ENTITY_TYPE_NAME = new EntityTypeNameAdapter();
    @Deprecated(forRemoval = true)
    EntityNameAdapter ENTITY_NAME = new EntityNameAdapter();

    @Deprecated(forRemoval = true)
    BlockTypeNameAdapter BLOCK_TYPE_NAME = new BlockTypeNameAdapter();
    @Deprecated(forRemoval = true)
    BlockTypeIdAdapter BLOCK_TYPE_ID = new BlockTypeIdAdapter();
    @Deprecated(forRemoval = true)
    PermissionNodeAdapter PERMISSION_NODE = new PermissionNodeAdapter();
    @Deprecated(forRemoval = true)
    InvalidNameAdapter INVALID_NAME = new InvalidNameAdapter();

    @Deprecated(forRemoval = true)
    StructureChunkSizeAdapter STRUCTURE_CHUNK_SIZE = new StructureChunkSizeAdapter();
    @Deprecated(forRemoval = true)
    StructureSizeAdapter STRUCTURE_SIZE = new StructureSizeAdapter();

    @Deprecated(forRemoval = true)
    NamedBlockNameAdapter NAMED_BLOCK_NAME = new NamedBlockNameAdapter();
    @Deprecated(forRemoval = true)
    TrackLimitAdapter CONFIG_TRACK_LIMIT = new TrackLimitAdapter();
    @Deprecated(forRemoval = true)
    ItemNameAdapter ITEM_NAME = new ItemNameAdapter();
    @Deprecated(forRemoval = true)
    ItemIdAdapter ITEM_ID = new ItemIdAdapter();
    @Deprecated(forRemoval = true)
    NumberAdapter<Integer> FUEL_FOUND_REQUIREMENT = new NumberAdapter<>("Fuel found");
    @Deprecated(forRemoval = true)
    NumberAdapter<Integer> FUEL_CONSUMPTION_REQUIREMENT = new NumberAdapter<>("Fuel consumption");
    @Deprecated(forRemoval = true)
    NumberAdapter<Integer> FUEL_LEFT_REQUIREMENT = new NumberAdapter<>("Fuel left");
    @Deprecated(forRemoval = true)
    NumberAdapter<Integer> TOTAL_FOUND_BLOCKS = new NumberAdapter<>("total found blocks");
    @Deprecated(forRemoval = true)
    NumberAdapter<Double> PERCENT_FOUND = new NumberAdapter<>("Percent found");
    @Deprecated(forRemoval = true)
    BlockGroupIdAdapter BLOCK_IDS = new BlockGroupIdAdapter("block ids");
    @Deprecated(forRemoval = true)
    BlockGroupNameAdapter BLOCK_NAMES = new BlockGroupNameAdapter("block names");
    @Deprecated(forRemoval = true)
    List<MessageAdapter<ItemType>> ITEM_ADAPTERS = List.of(ITEM_NAME, ITEM_ID);

    @Deprecated(forRemoval = true)
    List<ConfigAdapter<?>> CONFIG_ADAPTERS = Collections.singletonList(CONFIG_TRACK_LIMIT);
    @Deprecated(forRemoval = true)
    List<MessageAdapter<BlockType>> BLOCK_TYPE_ADAPTERS = Arrays.asList(BLOCK_TYPE_ID, BLOCK_TYPE_NAME);
    @Deprecated(forRemoval = true)
    List<MessageAdapter<Position<?>>> LOCATION_ADAPTERS = new ArrayList<>() {{
        this.addAll(BLOCK_TYPE_ADAPTERS
                            .parallelStream()
                            .map(ma -> new MappedAdapter<Position<?>, BlockType>(BlockType.class, ma,
                                                                                 (Position::getBlockType)))
                            .collect(Collectors.toSet()));
    }};
    @Deprecated(forRemoval = true)
    List<MessageAdapter<EntityType<?, ?>>> ENTITY_TYPE_ADAPTERS = Arrays.asList(ENTITY_TYPE_ID, ENTITY_TYPE_NAME);

    @Deprecated(forRemoval = true)
    List<MessageAdapter<Entity<?>>> ENTITY_ADAPTERS = new ArrayList<>() {{
        this.add(ENTITY_NAME);
        this.addAll(LOCATION_ADAPTERS
                            .parallelStream()
                            .map(ma -> new MappedAdapter<Entity<?>, Position<?>>(Position.class, ma,
                                                                                 Entity::getPosition))
                            .collect(Collectors.toSet()));
        this.addAll(ENTITY_TYPE_ADAPTERS
                            .parallelStream()
                            .map(ma -> new MappedAdapter<Entity<?>, EntityType<?, ?>>(EntityType.class, ma,
                                                                                      Entity::getType))
                            .collect(Collectors.toSet()));
    }};
    @Deprecated(forRemoval = true)
    List<MessageAdapter<PositionableShipsStructure>> STRUCTURE_ADAPTERS = new ArrayList<>() {{
        this.add(STRUCTURE_SIZE);
        this.add(STRUCTURE_CHUNK_SIZE);
    }};


    List<MessageAdapter<Vessel>> VESSEL_ADAPTERS = new ArrayList<>() {{
        this.add(VESSEL_NAME);
        this.add(VESSEL_SPEED);
        this.add(VESSEL_ID);
        this.addAll(STRUCTURE_ADAPTERS
                            .parallelStream()
                            .map(ma -> new MappedAdapter<>(PositionableShipsStructure.class, ma, Vessel::getStructure))
                            .collect(Collectors.toSet()));
        this.addAll(LOCATION_ADAPTERS
                            .parallelStream()
                            .map(ma -> new MappedAdapter<>(Position.class, ma, Vessel::getPosition))
                            .collect(Collectors.toSet()));
    }};

    String[] getPath();

    Component getDefaultMessage();

    Collection<AdapterCategory<?>> getCategories();

    default Collection<MessageAdapter<?>> getAdapters() {
        return this
                .getCategories()
                .parallelStream()
                .flatMap(MessageAdapters::getAdaptersFor)
                .distinct()
                .map(adapter -> (MessageAdapter<?>) adapter)
                .collect(Collectors.toCollection(LinkedTransferQueue::new));
    }


    Component processMessage(@NotNull Component text, R obj);

    default Component processMessage(R obj) {
        return this.processMessage(this.parseMessage(), obj);
    }

    default ConfigurationNode.KnownParser.SingleKnown<Component> getConfigNode() {
        return new ConfigurationNode.KnownParser.SingleKnown<>(StringParser.STRING_TO_COMPONENT, this.getPath());
    }

    default Component parseMessage(AdventureMessageConfig config) {
        return config.getFile().parse(this.getConfigNode()).orElse(this.getDefaultMessage());
    }

    default Component parseMessage() {
        return this.parseMessage(ShipsPlugin.getPlugin().getAdventureMessageConfig());
    }

    default Collection<String> suggestAdapter(String peek) {
        String peekLower = peek.replaceAll("%", "").toLowerCase();
        return this
                .getAdapters()
                .parallelStream()
                .map(a -> a.adapterText().toLowerCase())
                .filter(a -> a.contains(peekLower))
                .collect(Collectors.toSet());
    }

    @SafeVarargs
    static <T> CollectionSingleAdapter<T> asCollectionSingle(MessageAdapter<T>... array) {
        return asCollectionSingle(Arrays.asList(array));
    }

    static <T> CollectionSingleAdapter<T> asCollectionSingle(Collection<? extends MessageAdapter<T>> collection) {
        return new CollectionSingleAdapter<>(collection);
    }
}
