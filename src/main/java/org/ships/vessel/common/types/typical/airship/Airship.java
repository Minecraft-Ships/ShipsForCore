package org.ships.vessel.common.types.typical.airship;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.config.parser.parsers.StringToEnumParser;
import org.core.inventory.item.ItemType;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.exceptions.NoLicencePresent;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.assits.FallableRequirementVessel;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.requirement.*;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;
import java.util.stream.Collectors;

public class Airship extends AbstractShipsVessel implements AirType, FallableRequirementVessel {

    protected final ConfigurationNode.KnownParser.SingleKnown<Boolean> configBurnerBlock = new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_BOOLEAN, "Block", "Burner");
    protected final ConfigurationNode.KnownParser.SingleKnown<Double> configSpecialBlockPercent = new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_DOUBLE, "Block", "Special", "Percent");
    protected final ConfigurationNode.KnownParser.CollectionKnown<BlockType> configSpecialBlockType = new ConfigurationNode.KnownParser.CollectionKnown<>(
            Parser.STRING_TO_BLOCK_TYPE, "Block", "Special", "Type");
    protected final ConfigurationNode.KnownParser.SingleKnown<Integer> configFuelConsumption = new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_INTEGER, "Block", "Fuel", "Consumption");
    protected final ConfigurationNode.KnownParser.SingleKnown<FuelSlot> configFuelSlot = new ConfigurationNode.KnownParser.SingleKnown<>(
            new StringToEnumParser<>(FuelSlot.class), "Block", "Fuel", "Slot");
    protected final ConfigurationNode.KnownParser.CollectionKnown<ItemType> configFuelTypes = new ConfigurationNode.KnownParser.CollectionKnown<>(
            Parser.STRING_TO_ITEM_TYPE, "Block", "Fuel", "Types");

    private final Collection<Requirement<?>> requirements = new HashSet<>();

    public Airship(ShipType<? extends Airship> type, LiveTileEntity licence) throws NoLicencePresent {
        super(licence, type);
        this.initRequirements();
    }

    public Airship(ShipType<? extends Airship> type, SignTileEntity ste, SyncBlockPosition position) {
        super(ste, position, type);
        this.initRequirements();
    }

    public MaxSizeRequirement getMaxBlocksRequirement() {
        return this
                .getRequirement(MaxSizeRequirement.class)
                .orElseThrow(() -> new RuntimeException("Submarine is missing a max blocks requirement"));
    }

    public MinSizeRequirement getMinBlocksRequirement() {
        return this
                .getRequirement(MinSizeRequirement.class)
                .orElseThrow(() -> new RuntimeException("Submarine is missing a min blocks requirement"));
    }

    public void setBurner(@Nullable Boolean check) {
        SpecialBlockRequirement requirement = this.getSpecialBlockRequirement();
        SpecialBlockRequirement copyRequirement = requirement.createCopyWithAmount((check != null && check) ? 1 : 0);
        this.setRequirement(copyRequirement);
    }

    public SpecialBlocksRequirement getSpecialBlocksRequirement() {
        return this
                .getRequirement(SpecialBlocksRequirement.class)
                .orElseThrow(() -> new RuntimeException("Airship is missing the special blocks requirement"));
    }

    public SpecialBlockRequirement getSpecialBlockRequirement() {
        return this
                .getRequirement(SpecialBlockRequirement.class)
                .orElseThrow(() -> new RuntimeException("Airship is missing the special single block requirement"));
    }

    public FuelRequirement getFuelRequirement() {
        return this
                .getRequirement(FuelRequirement.class)
                .orElseThrow(() -> new RuntimeException("Airship is missing the fuel requirement"));
    }

    @Override
    public Collection<Requirement<?>> getRequirements() {
        return Collections.unmodifiableCollection(this.requirements);
    }

    public boolean isUsingBurner() {
        return this.getSpecialBlockRequirement().isEnabled();
    }

    public boolean isSpecialBlocksPercentSpecified() {
        return this.getSpecialBlocksRequirement().isPercentageSpecified();
    }

    public float getSpecialBlocksPercent() {
        return this.getSpecialBlocksRequirement().getPercentage();
    }

    public void setSpecialBlocksPercent(@Nullable Float percent) {
        SpecialBlocksRequirement requirement = this.getSpecialBlocksRequirement();
        SpecialBlocksRequirement copyRequirement = requirement.createCopyWithPercentage(percent);
        this.setRequirement(copyRequirement);
    }

    public int getFuelConsumption() {
        return this.getFuelRequirement().getConsumption();
    }

    public @NotNull Vessel setMaxSize(@Nullable Integer size) {
        MaxSizeRequirement maxRequirements = this.getMaxBlocksRequirement();
        maxRequirements = maxRequirements.createCopy(size);
        this.setRequirement(maxRequirements);
        return this;
    }

    public boolean isMaxSizeSpecified() {
        return this.getMaxBlocksRequirement().isMaxSizeSpecified();
    }

    public @NotNull Vessel setMinSize(@Nullable Integer size) {
        MinSizeRequirement minRequirements = this.getMinBlocksRequirement();
        minRequirements = minRequirements.createCopy(size);
        this.setRequirement(minRequirements);
        return this;
    }

    public int getMinSize() {
        return this.getMinBlocksRequirement().getMinimumSize();
    }

    public boolean isMinSizeSpecified() {
        return this.getMinBlocksRequirement().isMinSizeSpecified();
    }

    public @NotNull Airship setFuelConsumption(@Nullable Integer fuel) {
        FuelRequirement requirement = this.getFuelRequirement();
        FuelRequirement copyRequirement = requirement.createCopyWithConsumption(fuel);
        this.setRequirement(copyRequirement);
        return this;
    }

    public FuelSlot getFuelSlot() {
        return this.getFuelRequirement().getFuelSlot();
    }

    public Airship setFuelSlot(FuelSlot check) {
        FuelRequirement requirement = this.getFuelRequirement();
        FuelRequirement copyRequirement = requirement.createCopyWithSlot(check);
        this.setRequirement(copyRequirement);
        return this;
    }

    public Collection<BlockType> getSpecialBlocks() {
        return this.getSpecialBlocksRequirement().getBlocks();
    }

    public void setSpecialBlocks(@Nullable Collection<BlockType> types) {
        SpecialBlocksRequirement requirement = this.getSpecialBlocksRequirement();
        SpecialBlocksRequirement copyRequirement = requirement.createCopyWithBlocks(types);
        this.setRequirement(copyRequirement);
    }

    public boolean isSpecialBlocksSpecified() {
        return this.getSpecialBlocksRequirement().isBlocksSpecified();
    }

    public Collection<ItemType> getFuelTypes() {
        return this.getFuelRequirement().getFuelTypes();
    }

    public void setFuelTypes(@Nullable Collection<ItemType> types) {
        FuelRequirement requirement = this.getFuelRequirement();
        FuelRequirement copyRequirement = requirement.createCopyWithFuel(types);
        this.setRequirement(copyRequirement);
    }

    @Override
    public @NotNull AirshipType getType() {
        return (AirshipType) super.getType();
    }

    @Override
    public void setRequirement(Requirement<?> updated) {
        this.getRequirement(updated.getClass()).ifPresent(this.requirements::remove);
        this.requirements.add(updated);
    }

    @Override
    public @NotNull Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(@NotNull ConfigurationStream file) {
        AirshipType type = this.getType();
        FuelRequirement fuelRequirement = this.getFuelRequirement().getRequirementsBetween(type.getFuelRequirement());
        SpecialBlockRequirement burnerRequirement = this
                .getSpecialBlockRequirement()
                .getRequirementsBetween(type.getBurnerRequirement());
        SpecialBlocksRequirement specialBlockRequirement = this
                .getSpecialBlocksRequirement()
                .getRequirementsBetween(type.getSpecialBlocksRequirement());

        Collection<ItemType> fuelTypes = fuelRequirement.getSpecifiedFuelTypes();
        Collection<BlockType> specialBlocks = specialBlockRequirement.getSpecifiedBlocks();


        Map<ConfigurationNode.KnownParser<?, ?>, Object> map = new HashMap<>();
        fuelRequirement.getSpecifiedConsumption().ifPresent(value -> map.put(this.configFuelConsumption, value));
        fuelRequirement.getSpecifiedFuelSlot().ifPresent(slot -> map.put(this.configFuelTypes, slot));
        if (!fuelTypes.isEmpty()) {
            map.put(this.configFuelTypes, fuelTypes);
        }

        burnerRequirement.getSpecifiedBlock().ifPresent(value -> map.put(this.configBurnerBlock, value));

        specialBlockRequirement
                .getSpecifiedPercent()
                .ifPresent(value -> map.put(this.configSpecialBlockPercent, value));
        if (!specialBlocks.isEmpty()) {
            map.put(this.configSpecialBlockType, specialBlocks);
        }
        return map;
    }

    @Override
    public @NotNull Airship deserializeExtra(@NotNull ConfigurationStream file) {
        file.getBoolean(this.configBurnerBlock).ifPresent(this::setBurner);
        file.getDouble(this.configSpecialBlockPercent).ifPresent(v -> this.setSpecialBlocksPercent(v.floatValue()));
        file.getInteger(this.configFuelConsumption).ifPresent(this::setFuelConsumption);
        this.setFuelTypes(file.parseCollection(this.configFuelTypes, new HashSet<>()));
        this.setFuelSlot(file.parse(this.configFuelSlot).orElse(null));
        this.setSpecialBlocks(file.parseCollection(this.configSpecialBlockType, new HashSet<>(), null));
        return this;
    }

    @Override
    public @NotNull Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Fuel", this
                .getFuelTypes()
                .stream()
                .map(Parser.STRING_TO_ITEM_TYPE::unparse)
                .collect(Collectors.joining(", ")));
        map.put("Fuel Consumption", this.getFuelConsumption() + "");
        map.put("Fuel Slot", this.getFuelSlot().name());
        map.put("Special Block", this
                .getSpecialBlocks()
                .stream()
                .map(Parser.STRING_TO_BLOCK_TYPE::unparse)
                .collect(Collectors.joining(", ")));
        map.put("Required Percent", this.getSpecialBlocksPercent() + "");
        map.put("Requires Burner", this.isUsingBurner() + "");
        return map;
    }
}
