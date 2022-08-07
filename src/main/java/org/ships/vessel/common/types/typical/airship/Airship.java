package org.ships.vessel.common.types.typical.airship;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.config.parser.parsers.StringToEnumParser;
import org.core.inventory.inventories.general.block.FurnaceInventory;
import org.core.inventory.item.ItemType;
import org.core.inventory.item.ItemTypes;
import org.core.inventory.item.stack.ItemStack;
import org.core.inventory.parts.Slot;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
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
import org.ships.exceptions.MoveException;
import org.ships.exceptions.NoLicencePresent;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlock;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.movement.result.data.RequiredFuelMovementData;
import org.ships.movement.result.data.RequiredPercentMovementData;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.assits.Fallable;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.requirement.Requirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;
import java.util.stream.Collectors;

public class Airship extends AbstractShipsVessel implements AirType, Fallable, VesselRequirement {

    protected final ConfigurationNode.KnownParser.SingleKnown<Boolean> configBurnerBlock =
            new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_BOOLEAN, "Block", "Burner");
    protected final ConfigurationNode.KnownParser.SingleKnown<Double> configSpecialBlockPercent =
            new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_DOUBLE, "Block", "Special", "Percent");
    protected final ConfigurationNode.KnownParser.CollectionKnown<BlockType> configSpecialBlockType =
            new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_BLOCK_TYPE, "Block", "Special",
                    "Type");
    protected final ConfigurationNode.KnownParser.SingleKnown<Integer> configFuelConsumption =
            new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Block", "Fuel", "Consumption");
    protected final ConfigurationNode.KnownParser.SingleKnown<FuelSlot> configFuelSlot =
            new ConfigurationNode.KnownParser.SingleKnown<>(new StringToEnumParser<>(FuelSlot.class), "Block", "Fuel",
                    "Slot");
    protected final ConfigurationNode.KnownParser.CollectionKnown<ItemType> configFuelTypes =
            new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_ITEM_TYPE, "Block", "Fuel", "Types");
    private @Nullable Boolean useBurner;
    private @Nullable Float specialBlockPercent;
    private @Nullable Collection<BlockType> specialBlocks;
    private @Nullable Integer fuelConsumption;
    private @Nullable FuelSlot fuelSlot;
    private @Nullable Collection<ItemType> fuelTypes;

    public Airship(ShipType<? extends Airship> type, LiveTileEntity licence) throws NoLicencePresent {
        super(licence, type);
    }

    public Airship(ShipType<? extends Airship> type, SignTileEntity ste, SyncBlockPosition position) {
        super(ste, position, type);
    }

    @Override
    public Collection<Requirement> getRequirements() {
        throw new RuntimeException("Not implemented");
    }

    public boolean isUsingBurner() {
        if (this.useBurner == null) {
            return this.getType().isUsingBurner();
        }
        return this.useBurner;
    }

    public boolean isSpecialBlocksPercentSpecified() {
        return this.specialBlockPercent != null;
    }

    @Deprecated(forRemoval = true)
    public float getSpecialBlockPercent() {
        return this.getSpecialBlocksPercent();
    }

    @Deprecated(forRemoval = true)
    public @NotNull Airship setSpecialBlockPercent(@Nullable Float percent) {
        this.setSpecialBlocksPercent(percent);
        return this;
    }

    public float getSpecialBlocksPercent() {
        return Objects
                .requireNonNullElseGet(
                        this.specialBlockPercent,
                        () -> this.getType().getDefaultSpecialBlockPercent());
    }

    public void setSpecialBlocksPercent(@Nullable Float percent) {
        if (percent != null && (percent > 100 || percent < 0)) {
            throw new IndexOutOfBoundsException(percent + " must be between 0 and 100");
        }
        this.specialBlockPercent = percent;
    }

    public int getFuelConsumption() {
        return Objects.requireNonNullElseGet(this.fuelConsumption, () -> this.getType().getDefaultFuelConsumption());
    }

    public @NotNull Airship setFuelConsumption(@Nullable Integer fuel) {
        if (fuel != null && fuel < 0) {
            throw new IndexOutOfBoundsException("Fuel cannot be less then 0");
        }
        this.fuelConsumption = fuel;
        return this;
    }

    public FuelSlot getFuelSlot() {
        if (this.fuelSlot == null) {
            return this.getType().getDefaultFuelSlot();
        }
        return this.fuelSlot;
    }

    public Airship setFuelSlot(FuelSlot check) {
        this.fuelSlot = check;
        return this;
    }

    public Collection<BlockType> getSpecialBlocks() {
        if (this.specialBlocks == null) {
            return this.getType().getDefaultSpecialBlockType();
        }
        return this.specialBlocks;
    }

    public void setSpecialBlocks(@Nullable Collection<BlockType> types) {
        this.specialBlocks = types;
    }

    public boolean isSpecialBlocksSpecified() {
        return this.specialBlocks != null;
    }

    public Collection<ItemType> getFuelTypes() {
        if (this.fuelTypes == null) {
            return this.getType().getDefaultFuelTypes();
        }
        return this.fuelTypes;
    }

    public @NotNull AirshipType getType() {
        return (AirshipType) super.getType();
    }

    @Override
    @Deprecated(forRemoval = true)
    public void meetsRequirements(MovementContext context) throws MoveException {
        VesselRequirement.super.meetsRequirements(context);
        if (!context.isStrictMovement()) {
            return;
        }
        int specialBlockCount = 0;
        boolean burnerFound = false;
        Collection<FurnaceInventory> furnaceInventories = new HashSet<>();
        for (MovingBlock movingBlock : context.getMovingStructure()) {
            SyncBlockPosition blockPosition = movingBlock.getBeforePosition();
            BlockType positionType = blockPosition.getBlockType();
            BlockDetails details = movingBlock.getStoredBlockData();
            if (blockPosition.getBlockType().equals(BlockTypes.FIRE)) {
                burnerFound = true;
            }
            if (this.getSpecialBlocks().parallelStream().anyMatch(b -> b.equals(positionType))) {
                specialBlockCount++;
            }
            Optional<TileEntitySnapshot<? extends TileEntity>> opTiled = details.get(KeyedData.TILED_ENTITY);
            if (opTiled.isPresent()) {
                if (opTiled.get() instanceof FurnaceTileEntitySnapshot ftes) {
                    furnaceInventories.add(ftes.getInventory());
                }
            }
        }
        if (this.isUsingBurner() && !burnerFound) {
            throw new MoveException(
                    new AbstractFailedMovement<>(this, MovementResult.NO_SPECIAL_NAMED_BLOCK_FOUND, "Burner"));
        }
        float specialBlockPercent = ((specialBlockCount * 100.0f) / context.getMovingStructure().size());
        if ((this.getSpecialBlockPercent() != 0) && specialBlockPercent <= this.getSpecialBlockPercent()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_PERCENT,
                    new RequiredPercentMovementData(this.getSpecialBlocks().iterator().next(),
                            this.getSpecialBlockPercent(), specialBlockPercent)));
        }
        if (!(this.getFuelConsumption() == 0 || this.getFuelTypes().isEmpty())) {
            boolean acceptedSlots = furnaceInventories
                    .stream()
                    .map(i -> this.getFuelSlot() == FuelSlot.TOP ? i.getSmeltingSlot() : i.getFuelSlot())
                    .filter(slot -> slot.getItem().isPresent())
                    .filter(slot -> slot.getItem().map(ItemStack::getQuantity).orElse(0) >= this.getFuelConsumption())
                    .anyMatch(slot -> this
                            .getFuelTypes()
                            .stream()
                            .anyMatch(type -> slot.getItem().map(t -> t.getType().equals(type)).orElse(false)));
            if (!acceptedSlots) {
                throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_FUEL,
                        new RequiredFuelMovementData(this.getFuelConsumption(), this.getFuelTypes())));
            }
        }
    }

    @Override
    @Deprecated(forRemoval = true)
    public void processRequirements(MovementContext context) throws MoveException {
        VesselRequirement.super.processRequirements(context);
        if (!context.isStrictMovement()) {
            return;
        }
        Collection<FurnaceInventory> furnaceInventories = new HashSet<>();
        for (MovingBlock movingBlock : context.getMovingStructure()) {
            BlockDetails details = movingBlock.getStoredBlockData();
            Optional<TileEntitySnapshot<? extends TileEntity>> opTiled = details.get(KeyedData.TILED_ENTITY);
            if (opTiled.isPresent()) {
                if (opTiled.get() instanceof FurnaceTileEntitySnapshot) {
                    furnaceInventories.add(((FurnaceTileEntity) opTiled.get()).getInventory());
                }
            }
        }
        if (!(this.getFuelConsumption() == 0 && this.getFuelTypes().isEmpty())) {
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
            }).collect(Collectors.toList());
            if (acceptedSlots.isEmpty()) {
                throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_FUEL,
                        new RequiredFuelMovementData(this.getFuelConsumption(), this.getFuelTypes())));
            }
            FurnaceInventory inv = acceptedSlots.get(0);
            Slot slot = this.getFuelSlot() == FuelSlot.TOP ? inv.getSmeltingSlot() : inv.getFuelSlot();
            Optional<ItemStack> opItem = slot.getItem();
            if (!opItem.isPresent()) {
                return;
            }
            ItemStack item = opItem.get();
            item = item.copyWithQuantity(item.getQuantity() - this.getFuelConsumption());
            if (item.getQuantity() == 0) {
                item = ItemTypes.AIR.get().getDefaultItemStack();
            }
            slot.setItem(item);
        }
    }

    @Override
    public void setRequirement(Requirement updated) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(ConfigurationStream file) {
        Map<ConfigurationNode.KnownParser<?, ?>, Object> map = new HashMap<>();
        map.put(this.configBurnerBlock, this.isUsingBurner());
        map.put(this.configSpecialBlockType, this.getSpecialBlocks());
        map.put(this.configSpecialBlockPercent, this.getSpecialBlockPercent());
        map.put(this.configFuelConsumption, this.getFuelConsumption());
        map.put(this.configFuelSlot, this.getFuelSlot());
        map.put(this.configFuelTypes, this.getFuelTypes());
        return map;
    }

    @Override
    public AbstractShipsVessel deserializeExtra(ConfigurationStream file) {
        file.getBoolean(this.configBurnerBlock).ifPresent(v -> this.useBurner = v);
        file.getDouble(this.configSpecialBlockPercent).ifPresent(v -> this.specialBlockPercent = v.floatValue());
        file.getInteger(this.configFuelConsumption).ifPresent(v -> this.fuelConsumption = v);
        this.fuelTypes = file.parseCollection(this.configFuelTypes, new HashSet<>());
        this.specialBlocks = file.parseCollection(this.configSpecialBlockType, new HashSet<>());
        this.fuelSlot = file.parse(this.configFuelSlot).orElse(null);
        return this;
    }

    @Override
    public @NotNull Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Fuel",
                this
                        .getFuelTypes()
                        .stream()
                        .map(Parser.STRING_TO_ITEM_TYPE::unparse)
                        .collect(Collectors.joining(", ")));
        map.put("Fuel Consumption", this.getFuelConsumption() + "");
        map.put("Fuel Slot", this.getFuelSlot().name());
        map.put("Special Block", this.getSpecialBlocks().stream().map(Parser.STRING_TO_BLOCK_TYPE::unparse
        ).collect(Collectors.joining(", ")));
        map.put("Required Percent", this.getSpecialBlockPercent() + "");
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
        float specialBlockPercent = ((specialBlockCount * 100.0f) / this.getStructure().getOriginalRelativePositions().size());
        if ((this.getSpecialBlockPercent() != 0) && specialBlockPercent <= this.getSpecialBlockPercent()) {
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
            }).collect(Collectors.toList());
            return acceptedSlots.isEmpty();
        }
        return false;
    }
}
