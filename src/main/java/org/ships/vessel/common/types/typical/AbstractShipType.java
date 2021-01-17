package org.ships.vessel.common.types.typical;

import org.array.utils.ArrayUtils;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.config.parser.parsers.StringToEnumParser;
import org.core.inventory.item.ItemType;
import org.core.platform.Plugin;
import org.core.world.position.block.BlockType;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.config.parsers.VesselFlagWrappedParser;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.shiptype.SerializableShipType;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.Vessel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractShipType<V extends Vessel> implements SerializableShipType<V> {

    protected final String displayName;
    protected final Plugin plugin;
    protected final Set<VesselFlag<?>> flags = new HashSet<>();
    protected final BlockType[] types;
    protected final ExpandedBlockList blockList;
    protected final ConfigurationStream.ConfigurationFile file;

    public static final ConfigurationNode.KnownParser.CollectionKnown<BlockType, Set<BlockType>> SPECIAL_BLOCK_TYPE = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_BLOCK_TYPE, "Special", "Block", "Type");
    public static final ConfigurationNode.KnownParser.SingleKnown<Double> SPECIAL_BLOCK_PERCENT = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_DOUBLE, "Special", "Block", "Percent");

    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> FUEL_CONSUMPTION = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Block", "Fuel", "Consumption");
    public static final ConfigurationNode.KnownParser.SingleKnown<FuelSlot> FUEL_SLOT = new ConfigurationNode.KnownParser.SingleKnown<>(new StringToEnumParser<>(FuelSlot.class), "Block", "Fuel", "Slot");
    public static final ConfigurationNode.KnownParser.CollectionKnown<ItemType, Set<ItemType>> FUEL_TYPES = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_ITEM_TYPE, "Block", "Fuel", "Types");

    public static final ConfigurationNode.KnownParser.SingleKnown<Boolean> BURNER_BLOCK = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_BOOLEAN, "Block", "Burner");


    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> MAX_SPEED = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Speed", "Max");
    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> ALTITUDE_SPEED = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Speed", "Altitude");
    public static final ConfigurationNode.GroupKnown<VesselFlag<?>> META_FLAGS = new ConfigurationNode.GroupKnown<>(() -> ShipsPlugin
            .getPlugin()
            .getVesselFlags()
            .entrySet()
            .stream()
            .collect(Collectors.<Map.Entry<String, VesselFlag.Builder<?, ?>>, String, Parser<String, VesselFlag<?>>>toMap(e -> e.getKey().replaceAll(":", " "), new Function<Map.Entry<String, VesselFlag.Builder<?, ?>>, VesselFlagWrappedParser<?>>() {


                @Override
                public VesselFlagWrappedParser<?> apply(Map.Entry<String, VesselFlag.Builder<?, ?>> stringVesselFlagEntry) {
                    return build(stringVesselFlagEntry.getValue());
                }

                private <T> VesselFlagWrappedParser<?> build(VesselFlag.Builder<?, ?> builder) {
                    return new VesselFlagWrappedParser<>((VesselFlag.Builder<T, VesselFlag<T>>) builder);
                }
            })), i -> i.getId().replaceAll(":", " "), "Meta", "Flags");

    protected abstract void createDefault(ConfigurationStream.ConfigurationFile file);

    public AbstractShipType(Plugin plugin, String displayName, ConfigurationStream.ConfigurationFile file, BlockType... types){
        if(plugin == null){
            throw new NullPointerException("ShipType constructor failed: Plugin cannot be null");
        }
        if(displayName == null){
            throw new NullPointerException("ShipType constructor failed: DisplayName cannot be null");
        }
        if(file == null){
            throw new NullPointerException("ShipType constructor failed: File cannot be null");
        }
        if(types.length == 0){
            throw new NullPointerException("ShipType constructor failed: Type cannot be empty");
        }
        this.plugin = plugin;
        this.displayName = displayName;
        this.types = types;
        this.file = file;
        this.blockList = new ExpandedBlockList(getFile(), ShipsPlugin.getPlugin().getBlockList());
        if(!this.file.getFile().exists()){
            this.createDefault(this.file);
            this.file.save();
        }
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public ExpandedBlockList getDefaultBlockList() {
        return this.blockList;
    }

    @Override
    public ConfigurationStream.ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public int getDefaultMaxSpeed() {
        return this.getFile().parse(MAX_SPEED, Parser.STRING_TO_INTEGER).get();
    }

    @Override
    public int getDefaultAltitudeSpeed() {
        return this.getFile().parse(ALTITUDE_SPEED, Parser.STRING_TO_INTEGER).get();
    }

    @Override
    public BlockType[] getIgnoredTypes() {
        return this.types;
    }

    @Override
    public Set<VesselFlag<?>> getFlags() {
        return this.flags;
    }

    @Override
    public String getName() {
        String name = this.getFile().getFile().getName();
        String[] split = name.split(Pattern.quote("."));
        int length = split.length;
        name = ArrayUtils.toString(".", t -> t, ArrayUtils.filter(0, length - 1, split));
        return name;
    }

    @Override
    public void setMaxSpeed(int speed) {
        this.getFile().set(MAX_SPEED, speed);
    }

    @Override
    public void setAltitudeSpeed(int speed) {
        this.getFile().set(ALTITUDE_SPEED, speed);
    }

    @Override
    public void register(VesselFlag<?> flag) {
        this.flags.add(flag);
    }

    @Override
    public void save() {
        this.getFile().set(META_FLAGS, this.flags);
        this.getFile().save();
    }
}
