package org.ships.vessel.common.types.typical;

import org.array.utils.ArrayUtils;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.config.parser.parsers.StringToEnumParser;
import org.core.inventory.item.ItemType;
import org.core.platform.plugin.Plugin;
import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.shiptype.SerializableShipType;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.Vessel;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class AbstractShipType<V extends Vessel> implements SerializableShipType<V> {

    public static final ConfigurationNode.KnownParser.CollectionKnown<BlockType> SPECIAL_BLOCK_TYPE = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_BLOCK_TYPE, "Special", "Block", "Type");
    public static final ConfigurationNode.KnownParser.SingleKnown<Double> SPECIAL_BLOCK_PERCENT = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_DOUBLE, "Special", "Block", "Percent");
    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> FUEL_CONSUMPTION = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Block", "Fuel", "Consumption");
    public static final ConfigurationNode.KnownParser.SingleKnown<FuelSlot> FUEL_SLOT = new ConfigurationNode.KnownParser.SingleKnown<>(new StringToEnumParser<>(FuelSlot.class), "Block", "Fuel", "Slot");
    public static final ConfigurationNode.KnownParser.CollectionKnown<ItemType> FUEL_TYPES = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_ITEM_TYPE, "Block", "Fuel", "Types");
    public static final ConfigurationNode.KnownParser.SingleKnown<Boolean> BURNER_BLOCK = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_BOOLEAN, "Block", "Burner");
    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> MAX_SIZE = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Block", "Count", "Max");
    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> MIN_SIZE = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Block", "Count", "Min");
    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> MAX_SPEED = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Speed", "Max");
    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> ALTITUDE_SPEED = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Speed", "Altitude");
    protected final @NotNull String displayName;
    protected final @NotNull Plugin plugin;
    protected final @NotNull Set<VesselFlag<?>> flags = new HashSet<>();
    protected final @NotNull BlockType[] types;
    protected final @NotNull ConfigurationStream.ConfigurationFile file;

    public AbstractShipType(@NotNull Plugin plugin, @NotNull String displayName, @NotNull ConfigurationStream.ConfigurationFile file, BlockType... types) {
        if (types.length==0) {
            throw new NullPointerException("ShipType constructor failed: Type cannot be empty");
        }
        this.plugin = plugin;
        this.displayName = displayName;
        this.types = types;
        this.file = file;
        if (!this.file.getFile().exists()) {
            this.createDefault(this.file);
            this.file.save();
        }
    }

    protected abstract void createDefault(@NotNull ConfigurationStream.ConfigurationFile file);

    @Override
    public @NotNull String getDisplayName() {
        return this.displayName;
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    @Deprecated
    public @NotNull ExpandedBlockList getDefaultBlockList() {
        throw new RuntimeException("Use normal blockList now");
    }

    @Override
    public @NotNull ConfigurationStream.ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public int getDefaultMaxSpeed() {
        return this.getFile().parse(MAX_SPEED, Parser.STRING_TO_INTEGER).orElseThrow(() -> new IllegalStateException("Could not get max speed from " + this.displayName));
    }

    @Override
    public int getDefaultAltitudeSpeed() {
        return this.getFile().parse(ALTITUDE_SPEED, Parser.STRING_TO_INTEGER).orElseThrow(() -> new IllegalStateException("Could not get altitude speed from " + this.displayName));
    }

    @Override
    public @NotNull Optional<Integer> getDefaultMaxSize() {
        return this.getFile().parse(MAX_SIZE, Parser.STRING_TO_INTEGER);
    }

    @Override
    public int getDefaultMinSize() {
        return this.getFile().parse(MIN_SIZE, Parser.STRING_TO_INTEGER).orElse(0);
    }

    @Override
    public BlockType[] getIgnoredTypes() {
        return this.types;
    }

    @Override
    public @NotNull Set<VesselFlag<?>> getFlags() {
        return this.flags;
    }

    @Override
    public String getName() {
        String name = this.getFile().getFile().getName();
        String[] split = name.split(Pattern.quote("."));
        int length = split.length;
        name = String.join(".", ArrayUtils.filter(0, length - 1, split));
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
        ConfigurationStream.ConfigurationFile file = this.getFile();
        this.getDefaultMaxSize().ifPresent((size -> file.set(MAX_SIZE, size)));
        file.set(MIN_SIZE, this.getDefaultMinSize());

        this.getFlags().stream().filter(v -> v instanceof VesselFlag.Serializable).forEach(vf -> {
            String value = ((VesselFlag.Serializable<?>) vf).serialize();
            String[] id = vf.getId().split(Pattern.quote(":"));
            file.set(new ConfigurationNode("flags", id[0], id[1]), value);
        });

        file.save();
    }

    public void initFlags() {
        ConfigurationStream.ConfigurationFile file = this.getFile();
        this.getFlags().stream().filter(v -> v instanceof VesselFlag.Serializable).forEach(vf -> {
            VesselFlag.Serializable<?> vesselFlag = (VesselFlag.Serializable<?>) vf;
            String[] id = vf.getId().split(Pattern.quote(":"));
            Optional<String> opValue = file.getString(new ConfigurationNode("flags", id[0], id[1]));
            if (!opValue.isPresent()) {
                return;
            }
            if (!vesselFlag.isDeserializable(opValue.get())) {
                return;
            }
            vesselFlag.deserialize(opValue.get());
        });
    }
}
