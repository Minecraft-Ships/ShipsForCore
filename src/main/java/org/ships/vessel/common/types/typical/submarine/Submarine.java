package org.ships.vessel.common.types.typical.submarine;

import org.array.utils.ArrayUtils;
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
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.UnderWaterType;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.requirement.*;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;

public class Submarine extends AbstractShipsVessel implements UnderWaterType, VesselRequirement {

    protected final ConfigurationNode configBurnerBlock = new ConfigurationNode("Block", "Burner");
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
    private final Collection<Requirement> requirements = new HashSet<>();

    public Submarine(ShipType<? extends Submarine> type, LiveTileEntity licence) throws NoLicencePresent {
        super(licence, type);
        this.initRequirements();
    }

    public Submarine(ShipType<? extends Submarine> type, SignTileEntity ste, SyncBlockPosition position) {
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

    public SpecialBlocksRequirement getSpecialBlocksRequirement() {
        return this
                .getRequirement(SpecialBlocksRequirement.class)
                .orElseThrow(() -> new RuntimeException("Submarine is missing a Special blocks requirement"));
    }

    public FuelRequirement getFuelRequirement() {
        return this
                .getRequirement(FuelRequirement.class)
                .orElseThrow(() -> new RuntimeException("Submarine is missing a fuel requirement"));
    }

    public int getFuelConsumption() {
        return this.getFuelRequirement().getConsumption();
    }

    public FuelSlot getFuelSlot() {
        return this.getFuelRequirement().getFuelSlot();
    }

    public @NotNull Collection<BlockType> getSpecialBlocks() {
        return this.getSpecialBlocksRequirement().getBlocks();
    }

    public void setSpecialBlocks(@Nullable Collection<BlockType> types) {
        SpecialBlocksRequirement requirement = this.getSpecialBlocksRequirement();
        SpecialBlocksRequirement copy = requirement.createCopyWithBlocks(types);
        this.setRequirement(copy);
    }

    public float getSpecialBlocksPercent() {
        return this.getSpecialBlocksRequirement().getPercentage();
    }

    public void setSpecialBlocksPercent(@Nullable Float value) {
        SpecialBlocksRequirement requirement = this.getSpecialBlocksRequirement();
        SpecialBlocksRequirement copy = requirement.createCopyWithPercentage(value);
        this.setRequirement(copy);
    }

    public boolean isSpecialBlocksSpecified() {
        return this.getSpecialBlocksRequirement().isBlocksSpecified();
    }

    public boolean isSpecialBlocksPercentSpecified() {
        return this.getSpecialBlocksRequirement().isPercentageSpecified();
    }

    public @NotNull Collection<ItemType> getFuelTypes() {
        return this.getFuelRequirement().getFuelTypes();
    }

    @Override
    public @NotNull SubmarineType getType() {
        return (SubmarineType) super.getType();
    }

    @Override
    public @NotNull Vessel setMaxSize(@Nullable Integer size) {
        MaxSizeRequirement maxRequirements = this.getMaxBlocksRequirement();
        maxRequirements = maxRequirements.createCopy(size);
        this.setRequirement(maxRequirements);
        return this;
    }

    @Override
    public boolean isMaxSizeSpecified() {
        return this.getMaxBlocksRequirement().isMaxSizeSpecified();
    }

    @Override
    public @NotNull Vessel setMinSize(@Nullable Integer size) {
        MinSizeRequirement minRequirements = this.getMinBlocksRequirement();
        minRequirements = minRequirements.createCopy(size);
        this.setRequirement(minRequirements);
        return this;
    }

    @Override
    public boolean isMinSizeSpecified() {
        return this.getMinBlocksRequirement().isMinSizeSpecified();
    }

    @Override
    public @NotNull Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Fuel", ArrayUtils.toString(", ", Parser.STRING_TO_ITEM_TYPE::unparse, this.getFuelTypes()));
        map.put("Fuel Consumption", this.getFuelConsumption() + "");
        map.put("Fuel Slot", (this.getFuelSlot().name()));
        map.put("Special Block",
                ArrayUtils.toString(", ", Parser.STRING_TO_BLOCK_TYPE::unparse, this.getSpecialBlocks()));
        map.put("Required Percent", this.getSpecialBlocksPercent() + "");
        return map;
    }

    @Override
    public Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(@NotNull ConfigurationStream file) {
        Map<ConfigurationNode.KnownParser<?, ?>, Object> map = new HashMap<>();
        map.put(this.configSpecialBlockType, this.getSpecialBlocks());
        map.put(this.configSpecialBlockPercent, this.getSpecialBlocksPercent());
        map.put(this.configFuelConsumption, this.getFuelConsumption());
        map.put(this.configFuelSlot, this.getFuelSlot());
        map.put(this.configFuelTypes, this.getFuelTypes());
        return map;
    }

    @Override
    public AbstractShipsVessel deserializeExtra(@NotNull ConfigurationStream file) {
        SpecialBlocksRequirement specialBlocksRequirement = this.getSpecialBlocksRequirement();
        FuelRequirement fuelRequirement = this.getFuelRequirement();

        Optional<Double> opSpecialBlockPercent = file.getDouble(this.configSpecialBlockPercent);
        if (opSpecialBlockPercent.isPresent()) {
            specialBlocksRequirement = specialBlocksRequirement.createCopyWithPercentage(
                    opSpecialBlockPercent.get().floatValue());
        }
        specialBlocksRequirement = specialBlocksRequirement.createCopyWithBlocks(
                file.parseCollection(this.configSpecialBlockType, new HashSet<>()));
        this.setRequirement(specialBlocksRequirement);


        Optional<Integer> opFuelConsumption = file.getInteger(this.configFuelConsumption);
        if (opFuelConsumption.isPresent()) {
            fuelRequirement = fuelRequirement.createCopyWithConsumption(opFuelConsumption.get());
        }
        fuelRequirement = fuelRequirement.createCopyWithFuel(
                file.parseCollection(this.configFuelTypes, new HashSet<>()));
        fuelRequirement = fuelRequirement.createCopyWithSlot(file.parse(this.configFuelSlot).orElse(FuelSlot.BOTTOM));
        this.setRequirement(fuelRequirement);
        return this;
    }

    @Override
    public Collection<Requirement> getRequirements() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setRequirement(Requirement updated) {
        this.getRequirement(updated.getClass()).ifPresent(this.requirements::remove);
        this.requirements.add(updated);
    }
}
