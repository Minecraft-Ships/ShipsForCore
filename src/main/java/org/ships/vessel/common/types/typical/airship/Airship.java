package org.ships.vessel.common.types.typical.airship;

import org.array.utils.ArrayUtils;
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
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.block.entity.TileEntity;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.container.furnace.FurnaceTileEntitySnapshot;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.exceptions.MoveException;
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
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;
import java.util.stream.Collectors;

public class Airship extends AbstractShipsVessel implements AirType, Fallable, org.ships.vessel.common.assits.VesselRequirement {

    protected Boolean useBurner;
    protected Float specialBlockPercent;
    protected Set<BlockType> specialBlocks = new HashSet<>();
    protected Integer fuelConsumption;
    protected FuelSlot fuelSlot;
    protected Set<ItemType> fuelTypes = new HashSet<>();

    protected ConfigurationNode.KnownParser.SingleKnown<Boolean> configBurnerBlock = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_BOOLEAN, "Block", "Burner");
    protected ConfigurationNode.KnownParser.SingleKnown<Double> configSpecialBlockPercent = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_DOUBLE, "Block", "Special", "Percent");
    protected ConfigurationNode.KnownParser.CollectionKnown<BlockType, Set<BlockType>> configSpecialBlockType = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_BLOCK_TYPE, "Block", "Special", "Type");
    protected ConfigurationNode.KnownParser.SingleKnown<Integer> configFuelConsumption = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Block", "Fuel", "Consumption");
    protected ConfigurationNode.KnownParser.SingleKnown<FuelSlot> configFuelSlot = new ConfigurationNode.KnownParser.SingleKnown<>(new StringToEnumParser<>(FuelSlot.class), "Block", "Fuel", "Slot");
    protected ConfigurationNode.KnownParser.CollectionKnown<ItemType, Set<ItemType>> configFuelTypes = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_ITEM_TYPE, "Block", "Fuel", "Types");

    public Airship(AirshipType type, LiveSignTileEntity licence) {
        super(licence, type);
    }

    public Airship(AirshipType type, SignTileEntity ste, SyncBlockPosition position) {
        super(ste, position, type);
    }

    public boolean isUsingBurner() {
        if (this.useBurner == null) {
            return this.getType().isUsingBurner();
        }
        return this.useBurner;
    }

    public float getSpecialBlockPercent() {
        if (this.specialBlockPercent == null) {
            return this.getType().getDefaultSpecialBlockPercent();
        }
        return this.specialBlockPercent;
    }

    public Airship setSpecialBlockPercent(Float percent) {
        this.specialBlockPercent = percent;
        return this;
    }

    public int getFuelConsumption() {
        if (this.fuelConsumption == null) {
            return this.getType().getDefaultFuelConsumption();
        }
        return this.fuelConsumption;
    }

    public Airship setFuelConsumption(Integer fuel) {
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

    public Set<BlockType> getSpecialBlocks() {
        if (this.specialBlocks.isEmpty()) {
            return this.getType().getDefaultSpecialBlockType();
        }
        return this.specialBlocks;
    }

    public Set<ItemType> getFuelTypes() {
        if (this.fuelTypes.isEmpty()) {
            return this.getType().getDefaultFuelTypes();
        }
        return this.fuelTypes;
    }

    public AirshipType getType() {
        return (AirshipType) super.getType();
    }

    @Override
    public void meetsRequirements(MovementContext context) throws MoveException {
        VesselRequirement.super.meetsRequirements(context);
        if (!context.isStrictMovement()) {
            return;
        }
        int specialBlockCount = 0;
        boolean burnerFound = false;
        Set<FurnaceInventory> furnaceInventories = new HashSet<>();
        for (MovingBlock movingBlock : context.getMovingStructure()) {
            SyncBlockPosition blockPosition = movingBlock.getBeforePosition();
            BlockDetails details = movingBlock.getStoredBlockData();
            if (blockPosition.getBlockType().equals(BlockTypes.FIRE.get())) {
                burnerFound = true;
            }
            if (this.getSpecialBlocks().stream().anyMatch(b -> b.equals(blockPosition.getBlockType()))) {
                specialBlockCount++;
            }
            Optional<TileEntitySnapshot<? extends TileEntity>> opTiled = details.get(KeyedData.TILED_ENTITY);
            if (opTiled.isPresent()) {
                if (opTiled.get() instanceof FurnaceTileEntitySnapshot) {
                    furnaceInventories.add(((FurnaceTileEntitySnapshot) opTiled.get()).getInventory());
                }
            }
        }
        if (this.isUsingBurner() && !burnerFound) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NO_SPECIAL_NAMED_BLOCK_FOUND, "Burner"));
        }
        float specialBlockPercent = ((specialBlockCount * 100.0f) / context.getMovingStructure().size());
        if ((this.getSpecialBlockPercent() != 0) && specialBlockPercent <= this.getSpecialBlockPercent()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_PERCENT, new RequiredPercentMovementData(this.getSpecialBlocks().iterator().next(), this.getSpecialBlockPercent(), specialBlockPercent)));
        }
        if (!(this.getFuelConsumption() == 0 || this.getFuelTypes().isEmpty())) {
            List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.TOP) ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().isPresent();
            }).filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.TOP) ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().get().getQuantity() >= this.getFuelConsumption();
            }).filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.TOP) ? i.getSmeltingSlot() : i.getFuelSlot();
                return this.getFuelTypes().stream().anyMatch(type -> slot.getItem().get().getType().equals(type));
            }).collect(Collectors.toList());
            if (acceptedSlots.isEmpty()) {
                throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_FUEL, new RequiredFuelMovementData(this.getFuelConsumption(), this.getFuelTypes())));
            }
        }
    }

    @Override
    public void processRequirements(MovementContext context) throws MoveException {
        VesselRequirement.super.processRequirements(context);
        if (!context.isStrictMovement()) {
            return;
        }
        Set<FurnaceInventory> furnaceInventories = new HashSet<>();
        for (MovingBlock movingBlock : context.getMovingStructure()) {
            BlockDetails details = movingBlock.getStoredBlockData();
            Optional<TileEntitySnapshot<? extends TileEntity>> opTiled = details.get(KeyedData.TILED_ENTITY);
            if (opTiled.isPresent()) {
                if (opTiled.get() instanceof FurnaceTileEntitySnapshot) {
                    furnaceInventories.add(((FurnaceTileEntitySnapshot) opTiled.get()).getInventory());
                }
            }
        }
        if (!(this.getFuelConsumption() == 0 && this.getFuelTypes().isEmpty())) {
            List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.TOP) ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().isPresent();
            }).filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.TOP) ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().get().getQuantity() >= this.getFuelConsumption();
            }).filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.TOP) ? i.getSmeltingSlot() : i.getFuelSlot();
                return this.getFuelTypes().stream().anyMatch(type -> slot.getItem().get().getType().equals(type));
            }).collect(Collectors.toList());
            if (acceptedSlots.isEmpty()) {
                throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_FUEL, new RequiredFuelMovementData(this.getFuelConsumption(), this.getFuelTypes())));
            }
            FurnaceInventory inv = acceptedSlots.get(0);
            Slot slot = this.getFuelSlot().equals(FuelSlot.TOP) ? inv.getSmeltingSlot() : inv.getFuelSlot();
            ItemStack item = slot.getItem().get();
            item = item.copyWithQuantity(item.getQuantity() - this.getFuelConsumption());
            if (item.getQuantity() == 0) {
                item = null;
            }
            slot.setItem(item);
        }
    }

    @Override
    public Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(ConfigurationStream file) {
        Map<ConfigurationNode.KnownParser<?, ?>, Object> map = new HashMap<>();
        map.put(this.configBurnerBlock, this.isUsingBurner());
        map.put(this.configSpecialBlockType, this.getSpecialBlocks());
        map.put(this.configSpecialBlockPercent, this.getSpecialBlockPercent());
        map.put(this.configFuelConsumption, this.getFuelConsumption());
        map.put(this.configFuelSlot, this.getFuelSlot().equals(FuelSlot.TOP));
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
    public Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Fuel", ArrayUtils.toString(", ", Parser.STRING_TO_ITEM_TYPE::unparse, this.getFuelTypes()));
        map.put("Fuel Consumption", this.getFuelConsumption() + "");
        map.put("Fuel Slot", this.getFuelSlot().name());
        map.put("Special Block", ArrayUtils.toString(", ", Parser.STRING_TO_BLOCK_TYPE::unparse, this.getSpecialBlocks()));
        map.put("Required Percent", this.getSpecialBlockPercent() + "");
        map.put("Requires Burner", this.isUsingBurner() + "");
        return map;
    }

    @Override
    public boolean shouldFall() {
        int specialBlockCount = 0;
        boolean burnerFound = false;
        Set<FurnaceInventory> furnaceInventories = new HashSet<>();
        for (SyncBlockPosition position : this.getStructure().getPositions()) {
            if (position.getBlockType().equals(BlockTypes.FIRE.get())) {
                burnerFound = true;
            }
            if (this.getSpecialBlocks().stream().anyMatch(b -> b.equals(position.getBlockType()))) {
                specialBlockCount++;
            }
            Optional<TileEntitySnapshot<? extends TileEntity>> opTiled = position.getBlockDetails().get(KeyedData.TILED_ENTITY);
            if (opTiled.isPresent()) {
                if (opTiled.get() instanceof FurnaceTileEntitySnapshot) {
                    furnaceInventories.add(((FurnaceTileEntitySnapshot) opTiled.get()).getInventory());
                }
            }
        }
        if (this.isUsingBurner() && !burnerFound) {
            return false;
        }
        float specialBlockPercent = ((specialBlockCount * 100.0f) / getStructure().getPositions().size());
        if ((this.getSpecialBlockPercent() != 0) && specialBlockPercent <= this.getSpecialBlockPercent()) {
            return false;
        }
        if (this.getFuelConsumption() != 0 && (!this.getFuelTypes().isEmpty())) {
            List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.TOP) ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().isPresent();
            }).filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.TOP) ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().get().getQuantity() >= this.getFuelConsumption();
            }).filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.TOP) ? i.getSmeltingSlot() : i.getFuelSlot();
                return this.getFuelTypes().stream().anyMatch(type -> slot.getItem().get().getType().equals(type));
            }).collect(Collectors.toList());
            return !acceptedSlots.isEmpty();
        }
        return true;
    }
}
