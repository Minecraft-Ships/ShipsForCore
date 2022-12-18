package org.ships.vessel.common.types.typical.airship;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.config.parser.parsers.StringToEnumParser;
import org.core.inventory.inventories.general.block.FurnaceInventory;
import org.core.inventory.item.ItemType;
import org.core.inventory.item.stack.ItemStack;
import org.core.inventory.parts.Slot;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.TileEntity;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.container.furnace.FurnaceTileEntity;
import org.core.world.position.block.entity.container.furnace.FurnaceTileEntitySnapshot;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.exceptions.NoLicencePresent;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.assits.Fallable;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.requirement.*;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;
import java.util.stream.Collectors;

public class Airship extends AbstractShipsVessel implements AirType, Fallable, VesselRequirement {

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

    private final Collection<Requirement> requirements = new HashSet<>();

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
    public Collection<Requirement> getRequirements() {
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
    public void setRequirement(Requirement updated) {
        this.getRequirement(updated.getClass()).ifPresent(this.requirements::remove);
        this.requirements.add(updated);
    }

    @Override
    public Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(ConfigurationStream file) {
        Map<ConfigurationNode.KnownParser<?, ?>, Object> map = new HashMap<>();
        map.put(this.configBurnerBlock, this.isUsingBurner());
        map.put(this.configSpecialBlockType, this.getSpecialBlocks());
        map.put(this.configSpecialBlockPercent, this.getSpecialBlocksPercent());
        map.put(this.configFuelConsumption, this.getFuelConsumption());
        map.put(this.configFuelSlot, this.getFuelSlot());
        map.put(this.configFuelTypes, this.getFuelTypes());
        return map;
    }

    @Override
    public AbstractShipsVessel deserializeExtra(ConfigurationStream file) {
        file.getBoolean(this.configBurnerBlock).ifPresent(this::setBurner);
        file.getDouble(this.configSpecialBlockPercent).ifPresent(v -> this.setSpecialBlocksPercent(v.floatValue()));
        file.getInteger(this.configFuelConsumption).ifPresent(this::setFuelConsumption);
        this.setFuelTypes(file.parseCollection(this.configFuelTypes, new HashSet<>()));
        this.setFuelSlot(file.parse(this.configFuelSlot).orElse(null));
        this.setSpecialBlocks(file.parseCollection(this.configSpecialBlockType, new HashSet<>()));
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

    @Override
    public boolean shouldFall() {
        int specialBlockCount = 0;
        boolean burnerFound = false;
        Collection<FurnaceInventory> furnaceInventories = new HashSet<>();
        for (SyncBlockPosition position : this.getStructure().getSyncedPositions()) {
            if (position.getBlockType().equals(BlockTypes.FIRE)) {
                burnerFound = true;
            }
            if (this.getSpecialBlocks().stream().anyMatch(b -> b.equals(position.getBlockType()))) {
                specialBlockCount++;
            }
            Optional<TileEntitySnapshot<? extends TileEntity>> opTiled = position
                    .getBlockDetails()
                    .get(KeyedData.TILED_ENTITY);
            if (opTiled.isPresent()) {
                if (opTiled.get() instanceof FurnaceTileEntitySnapshot) {
                    furnaceInventories.add(((FurnaceTileEntity) opTiled.get()).getInventory());
                }
            }
        }
        if (this.isUsingBurner() && !burnerFound) {
            return true;
        }
        float specialBlockPercent = ((specialBlockCount * 100.0f) / this
                .getStructure()
                .getOriginalRelativePositions()
                .size());
        if ((this.getSpecialBlocksPercent() != 0) && specialBlockPercent <= this.getSpecialBlocksPercent()) {
            return true;
        }
        if (this.getFuelConsumption() != 0 && (!this.getFuelTypes().isEmpty())) {
            List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
                Slot slot = this.getFuelSlot() == FuelSlot.TOP ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().isPresent();
            }).filter(i -> {
                Slot slot = this.getFuelSlot() == FuelSlot.TOP ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().map(ItemStack::getQuantity).orElse(0) >= this.getFuelConsumption();
            }).filter(i -> {
                Slot slot = this.getFuelSlot() == FuelSlot.TOP ? i.getSmeltingSlot() : i.getFuelSlot();
                return this
                        .getFuelTypes()
                        .stream()
                        .anyMatch(type -> slot.getItem().map(item -> item.getType().equals(type)).orElse(false));
            }).toList();
            return acceptedSlots.isEmpty();
        }
        return false;
    }
}
