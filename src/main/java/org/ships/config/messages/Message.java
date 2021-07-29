package org.ships.config.messages;

import org.core.adventureText.AText;
import org.core.config.ConfigurationNode;
import org.core.config.parser.StringParser;
import org.core.entity.Entity;
import org.core.entity.EntityType;
import org.core.world.position.block.BlockType;
import org.core.world.position.impl.Position;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.block.BlockTypeIdAdapter;
import org.ships.config.messages.adapter.block.BlockTypeNameAdapter;
import org.ships.config.messages.adapter.config.ConfigAdapter;
import org.ships.config.messages.adapter.config.TrackLimitAdapter;
import org.ships.config.messages.adapter.entity.EntityNameAdapter;
import org.ships.config.messages.adapter.entity.type.EntityTypeIdAdapter;
import org.ships.config.messages.adapter.entity.type.EntityTypeNameAdapter;
import org.ships.config.messages.adapter.misc.CollectionSingleAdapter;
import org.ships.config.messages.adapter.misc.InvalidNameAdapter;
import org.ships.config.messages.adapter.misc.MappedAdapter;
import org.ships.config.messages.adapter.permission.PermissionNodeAdapter;
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
import org.ships.vessel.structure.ShipsStructure;

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

    TrackLimitAdapter CONFIG_TRACK_LIMIT = new TrackLimitAdapter();

    List<ConfigAdapter> CONFIG_ADAPTERS = Arrays.asList(CONFIG_TRACK_LIMIT);
    List<MessageAdapter<BlockType>> BLOCK_TYPE_ADAPTERS = Arrays.asList(BLOCK_TYPE_ID, BLOCK_TYPE_NAME);
    List<MessageAdapter<Position<?>>> LOCATION_ADAPTERS = new ArrayList<MessageAdapter<Position<?>>>() {{
        addAll(
                BLOCK_TYPE_ADAPTERS
                        .parallelStream()
                        .map(ma -> new MappedAdapter<Position<?>, BlockType>(ma, (Position::getBlockType)))
                        .collect(Collectors.toSet()));
    }};
    List<MessageAdapter<EntityType<?, ?>>> ENTITY_TYPE_ADAPTERS = Arrays.asList(ENTITY_TYPE_ID, ENTITY_TYPE_NAME);

    List<MessageAdapter<Entity<?>>> ENTITY_ADAPTERS = new ArrayList<MessageAdapter<Entity<?>>>(){{
        add(ENTITY_NAME);
        addAll(LOCATION_ADAPTERS.parallelStream().map(ma -> new MappedAdapter<Entity<?>, Position<?>>(ma, Entity::getPosition)).collect(Collectors.toSet()));
        addAll(ENTITY_TYPE_ADAPTERS.parallelStream().map(ma -> new MappedAdapter<Entity<?>, EntityType<?, ?>>(ma, Entity::getType)).collect(Collectors.toSet()));
    }};
    List<MessageAdapter<ShipsStructure>> STRUCTURE_ADAPTERS = new ArrayList<MessageAdapter<ShipsStructure>>(){{
        add(STRUCTURE_SIZE);
        add(STRUCTURE_CHUNK_SIZE);
    }};


    List<MessageAdapter<Vessel>> VESSEL_ADAPTERS = new ArrayList<MessageAdapter<Vessel>>() {{
        add(VESSEL_NAME);
        add(VESSEL_SPEED);
        add(VESSEL_SIZE);
        add(VESSEL_ID);
        addAll(STRUCTURE_ADAPTERS.parallelStream().map(ma -> new MappedAdapter<>(ma, Vessel::getStructure)).collect(Collectors.toSet()));
        addAll(LOCATION_ADAPTERS.parallelStream().map(ma -> new MappedAdapter<>(ma, Vessel::getPosition)).collect(Collectors.toSet()));
    }};



    String[] getPath();

    AText getDefault();

    Set<MessageAdapter<?>> getAdapters();

    AText process(AText text, R obj);

    default AText process(R obj) {
        return process(parse(), obj);
    }

    default ConfigurationNode.KnownParser.SingleKnown<AText> getKnownPath() {
        return new ConfigurationNode.KnownParser.SingleKnown<>(StringParser.STRING_TO_TEXT, this.getPath());
    }

    default AText parse(AdventureMessageConfig config) {
        return config.getFile().parse(this.getKnownPath()).orElse(this.getDefault());
    }

    default AText parse() {
        return parse(ShipsPlugin.getPlugin().getAdventureMessageConfig());
    }

    default Set<String> suggestAdapter(String peek) {
        String peekLower = peek.replaceAll("%", "").toLowerCase();
        return getAdapters().parallelStream().map(a -> a.adapterText().toLowerCase()).filter(a -> a.contains(peekLower)).collect(Collectors.toSet());
    }

    static <T> CollectionSingleAdapter<T> asCollectionSingle(MessageAdapter<T>... array) {
        return asCollectionSingle(Arrays.asList(array));
    }

    static <T> CollectionSingleAdapter<T> asCollectionSingle(Collection<MessageAdapter<T>> collection) {
        return new CollectionSingleAdapter<>(collection);
    }
}
