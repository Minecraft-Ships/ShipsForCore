package org.ships.config.messages;

import org.core.adventureText.AText;
import org.core.config.ConfigurationNode;
import org.core.config.parser.StringParser;
import org.core.entity.Entity;
import org.core.entity.EntityType;
import org.core.inventory.item.ItemType;
import org.core.world.position.block.BlockType;
import org.core.world.position.impl.Position;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.block.BlockTypeIdAdapter;
import org.ships.config.messages.adapter.block.BlockTypeNameAdapter;
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
import org.ships.config.messages.adapter.vessel.VesselSizeAdapter;
import org.ships.config.messages.adapter.vessel.VesselSpeedAdapter;
import org.ships.config.messages.adapter.vessel.crew.CrewIdAdapter;
import org.ships.config.messages.adapter.vessel.crew.CrewNameAdapter;
import org.ships.config.messages.adapter.vessel.error.VesselSizeErrorAdapter;
import org.ships.config.messages.adapter.vessel.flag.VesselFlagIdAdapter;
import org.ships.config.messages.adapter.vessel.flag.VesselFlagNameAdapter;
import org.ships.config.messages.adapter.vessel.info.VesselInfoKeyAdapter;
import org.ships.config.messages.adapter.vessel.info.VesselInfoValueAdapter;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;
import java.util.stream.Collectors;

public interface Message<R> {

    VesselIdAdapter VESSEL_ID = new VesselIdAdapter();
    VesselNameAdapter VESSEL_NAME = new VesselNameAdapter();
    VesselFlagIdAdapter VESSEL_FLAG_ID = new VesselFlagIdAdapter();
    VesselFlagNameAdapter VESSEL_FLAG_NAME = new VesselFlagNameAdapter();
    VesselInfoKeyAdapter VESSEL_INFO_KEY = new VesselInfoKeyAdapter();
    VesselInfoValueAdapter VESSEL_INFO_VALUE = new VesselInfoValueAdapter();

    VesselSpeedAdapter VESSEL_SPEED = new VesselSpeedAdapter();
    VesselSizeAdapter VESSEL_SIZE = new VesselSizeAdapter();
    CrewIdAdapter CREW_ID = new CrewIdAdapter();
    CrewNameAdapter CREW_NAME = new CrewNameAdapter();

    EntityTypeIdAdapter ENTITY_TYPE_ID = new EntityTypeIdAdapter();
    EntityTypeNameAdapter ENTITY_TYPE_NAME = new EntityTypeNameAdapter();

    EntityNameAdapter ENTITY_NAME = new EntityNameAdapter();

    BlockTypeNameAdapter BLOCK_TYPE_NAME = new BlockTypeNameAdapter();
    BlockTypeIdAdapter BLOCK_TYPE_ID = new BlockTypeIdAdapter();

    VesselSizeErrorAdapter VESSEL_SIZE_ERROR = new VesselSizeErrorAdapter();

    PermissionNodeAdapter PERMISSION_NODE = new PermissionNodeAdapter();

    InvalidNameAdapter INVALID_NAME = new InvalidNameAdapter();

    StructureChunkSizeAdapter STRUCTURE_CHUNK_SIZE = new StructureChunkSizeAdapter();
    StructureSizeAdapter STRUCTURE_SIZE = new StructureSizeAdapter();

    NamedBlockNameAdapter NAMED_BLOCK_NAME = new NamedBlockNameAdapter();
    TrackLimitAdapter CONFIG_TRACK_LIMIT = new TrackLimitAdapter();
    ItemNameAdapter ITEM_NAME = new ItemNameAdapter();
    ItemIdAdapter ITEM_ID = new ItemIdAdapter();
    NumberAdapter<Integer> FUEL_FOUND_REQUIREMENT = new NumberAdapter<>("Fuel found");
    NumberAdapter<Integer> FUEL_CONSUMPTION_REQUIREMENT = new NumberAdapter<>("Fuel consumption");
    NumberAdapter<Integer> FUEL_LEFT_REQUIREMENT = new NumberAdapter<>("Fuel left");
    NumberAdapter<Integer> TOTAL_FOUND_BLOCKS = new NumberAdapter<>("total found blocks");
    NumberAdapter<Double> PERCENT_FOUND = new NumberAdapter<>("Percent found");
    List<MessageAdapter<ItemType>> ITEM_ADAPTERS = List.of(ITEM_NAME, ITEM_ID);

    List<ConfigAdapter<?>> CONFIG_ADAPTERS = Collections.singletonList(CONFIG_TRACK_LIMIT);
    List<MessageAdapter<BlockType>> BLOCK_TYPE_ADAPTERS = Arrays.asList(BLOCK_TYPE_ID, BLOCK_TYPE_NAME);
    List<MessageAdapter<Position<?>>> LOCATION_ADAPTERS = new ArrayList<>() {{
        this.addAll(BLOCK_TYPE_ADAPTERS
                            .parallelStream()
                            .map(ma -> new MappedAdapter<Position<?>, BlockType>(ma, (Position::getBlockType)))
                            .collect(Collectors.toSet()));
    }};
    List<MessageAdapter<EntityType<?, ?>>> ENTITY_TYPE_ADAPTERS = Arrays.asList(ENTITY_TYPE_ID, ENTITY_TYPE_NAME);

    List<MessageAdapter<Entity<?>>> ENTITY_ADAPTERS = new ArrayList<>() {{
        this.add(ENTITY_NAME);
        this.addAll(LOCATION_ADAPTERS
                            .parallelStream()
                            .map(ma -> new MappedAdapter<Entity<?>, Position<?>>(ma, Entity::getPosition))
                            .collect(Collectors.toSet()));
        this.addAll(ENTITY_TYPE_ADAPTERS
                            .parallelStream()
                            .map(ma -> new MappedAdapter<Entity<?>, EntityType<?, ?>>(ma, Entity::getType))
                            .collect(Collectors.toSet()));
    }};
    List<MessageAdapter<PositionableShipsStructure>> STRUCTURE_ADAPTERS = new ArrayList<>() {{
        this.add(STRUCTURE_SIZE);
        this.add(STRUCTURE_CHUNK_SIZE);
    }};


    List<MessageAdapter<Vessel>> VESSEL_ADAPTERS = new ArrayList<>() {{
        this.add(VESSEL_NAME);
        this.add(VESSEL_SPEED);
        this.add(VESSEL_SIZE);
        this.add(VESSEL_ID);
        this.addAll(STRUCTURE_ADAPTERS
                            .parallelStream()
                            .map(ma -> new MappedAdapter<>(ma, Vessel::getStructure))
                            .collect(Collectors.toSet()));
        this.addAll(LOCATION_ADAPTERS
                            .parallelStream()
                            .map(ma -> new MappedAdapter<>(ma, Vessel::getPosition))
                            .collect(Collectors.toSet()));
    }};

    @SafeVarargs
    static <T> CollectionSingleAdapter<T> asCollectionSingle(MessageAdapter<T>... array) {
        return asCollectionSingle(Arrays.asList(array));
    }

    static <T> CollectionSingleAdapter<T> asCollectionSingle(Collection<? extends MessageAdapter<T>> collection) {
        return new CollectionSingleAdapter<>(collection);
    }

    String[] getPath();

    AText getDefault();

    Collection<MessageAdapter<?>> getAdapters();

    AText process(@NotNull AText text, R obj);

    default AText process(R obj) {
        return this.process(this.parse(), obj);
    }

    default ConfigurationNode.KnownParser.SingleKnown<AText> getKnownPath() {
        return new ConfigurationNode.KnownParser.SingleKnown<>(StringParser.STRING_TO_TEXT, this.getPath());
    }

    default AText parse(AdventureMessageConfig config) {
        return config.getFile().parse(this.getKnownPath()).orElse(this.getDefault());
    }

    default AText parse() {
        return this.parse(ShipsPlugin.getPlugin().getAdventureMessageConfig());
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
}
