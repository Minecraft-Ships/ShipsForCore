package org.ships.config.messages.adapter.category;

import org.core.entity.Entity;
import org.core.entity.EntityType;
import org.core.entity.EntityTypes;
import org.core.entity.living.human.player.LivePlayer;
import org.core.inventory.item.ItemType;
import org.core.world.position.block.BlockType;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.sync.SyncPosition;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.misc.CollectionAdapter;
import org.ships.config.messages.adapter.misc.MapToAdapter;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;

public final class AdapterCategories {

    public static final AdapterCategory<VesselFlag<?>> VESSEL_FLAG = new AbstractAdapterCategory<>(VesselFlag.class,
                                                                                                   "Vessel Flag");
    public static final AdapterCategory<PositionableShipsStructure> VESSEL_STRUCTURE = new AbstractAdapterCategory<>(
            PositionableShipsStructure.class, "Vessel Structure");
    public static final AdapterCategory<Vessel> VESSEL = new AbstractAdapterCategory<>(Vessel.class, "Vessel") {
        @Override
        public boolean canAccept(MessageAdapter<?> adapter) {
            if (VESSEL_FLAG.canAccept(adapter)) {
                return true;
            }
            if (VESSEL_STRUCTURE.canAccept(adapter)) {
                return true;
            }

            return super.canAccept(adapter);
        }

        @Override
        public MessageAdapter<Vessel> onAccept(MessageAdapter<?> adapter) {
            if (VESSEL_FLAG.canAccept(adapter)) {
                MessageAdapter<? super VesselFlag<?>> flagAdapter = VESSEL_FLAG.onAccept(adapter);
                return new MapToAdapter<>(Vessel.class, new CollectionAdapter<>(flagAdapter), vessel -> {
                    if (!(vessel instanceof ShipsVessel)) {
                        return Collections.emptyList();
                    }
                    return new ArrayList<>(((ShipsVessel)vessel).getFlags());
                });
            }
            if (VESSEL_STRUCTURE.canAccept(adapter)) {
                MessageAdapter<? super PositionableShipsStructure> structureAdapter = VESSEL_STRUCTURE.onAccept(
                        adapter);
                return new MapToAdapter<>(Vessel.class, structureAdapter, Vessel::getStructure);
            }
            return super.onAccept(adapter);
        }
    };
    public static final AdapterCategory<CrewPermission> CREW_PERMISSION = new AbstractAdapterCategory<>(
            CrewPermission.class, "Crew Permission");
    public static final AdapterCategory<Map.Entry<String, String>> VESSEL_INFO = new AbstractAdapterCategory<>(
            Map.Entry.class, "Vessel Info");

    public static final AdapterCategory<String> PERMISSION = new AbstractAdapterCategory<>(String.class, "Permission");
    public static final AdapterCategory<EntityType<?, ?>> ENTITY_TYPE = new AbstractAdapterCategory<>(EntityTypes.class,
                                                                                                      "Entity Type");

    public static final AdapterCategory<Entity<?>> ENTITY = new AbstractAdapterCategory<>(Entity.class, "Entity") {
        @Override
        public boolean canAccept(MessageAdapter<?> adapter) {
            if (ENTITY_TYPE.canAccept(adapter)) {
                return true;
            }
            return super.canAccept(adapter);
        }

        @Override
        public MessageAdapter<Entity<?>> onAccept(MessageAdapter<?> adapter) {
            if (ENTITY_TYPE.canAccept(adapter)) {
                return new MapToAdapter<>(Entity.class, ENTITY_TYPE.onAccept(adapter), entity -> entity.getType());
            }
            return super.onAccept(adapter);
        }
    };

    public static final AdapterCategory<LivePlayer> PLAYER = new AbstractAdapterCategory<>(LivePlayer.class, "Player") {
        @Override
        public boolean canAccept(MessageAdapter<?> adapter) {
            if (ENTITY.canAccept(adapter)) {
                return true;
            }
            return super.canAccept(adapter);
        }

        @Override
        public MessageAdapter<LivePlayer> onAccept(MessageAdapter<?> adapter) {
            if (ENTITY.canAccept(adapter)) {
                return new MapToAdapter<>(LivePlayer.class, ENTITY.onAccept(adapter), player -> player);
            }
            return super.onAccept(adapter);
        }
    };
    public static final AdapterCategory<BlockType> BLOCK_TYPE = new AbstractAdapterCategory<>(BlockType.class,
                                                                                              "Block Type");
    public static final AdapterCategory<Collection<BlockType>> BLOCK_GROUP = new AbstractAdapterCategory<>(List.class,
                                                                                                           "Block Group") {

        @Override
        public boolean canAccept(MessageAdapter<?> adapter) {
            if (adapter.categories().contains(BLOCK_TYPE)) {
                return true;
            }
            return super.canAccept(adapter);
        }

        @Override
        public MessageAdapter<Collection<BlockType>> onAccept(MessageAdapter<?> adapter) {
            if (BLOCK_TYPE.canAccept(adapter)) {
                MessageAdapter<BlockType> blockTypeAdapter = BLOCK_TYPE.onAccept(adapter);
                return new CollectionAdapter<>(blockTypeAdapter);
            }
            return super.onAccept(adapter);
        }
    };
    public static final AdapterCategory<ItemType> ITEM_TYPE = new AbstractAdapterCategory<>(ItemType.class,
                                                                                            "Item Type");

    public static final AdapterCategory<SyncPosition<? extends Number>> POSITION = new AbstractAdapterCategory<>(
            Position.class, "Position") {

        @Override
        public boolean canAccept(MessageAdapter<?> adapter) {
            if (BLOCK_TYPE.canAccept(adapter)) {
                return true;
            }
            return super.canAccept(adapter);
        }

        @Override
        public MessageAdapter<SyncPosition<?>> onAccept(MessageAdapter<?> adapter) {
            if (BLOCK_TYPE.canAccept(adapter)) {
                return new MapToAdapter<>(SyncPosition.class, BLOCK_TYPE.onAccept(adapter), Position::getBlockType);
            }
            return super.onAccept(adapter);
        }
    };


    private AdapterCategories() {
        throw new RuntimeException("Should not create");
    }
}
