package org.ships.vessel.common.types.typical.submarine;

import org.array.utils.ArrayUtils;
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
import org.core.world.position.block.entity.TileEntity;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.container.furnace.FurnaceTileEntity;
import org.core.world.position.block.entity.container.furnace.FurnaceTileEntitySnapshot;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
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
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.UnderWaterType;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;
import java.util.stream.Collectors;

public class Submarine extends AbstractShipsVessel implements UnderWaterType, org.ships.vessel.common.assits.VesselRequirement {

    protected @Nullable Float specialBlockPercent;
    protected @NotNull Set<BlockType> specialBlocks = new HashSet<>();
    protected @Nullable Integer fuelConsumption;
    protected @Nullable FuelSlot fuelSlot;
    protected Set<ItemType> fuelTypes = new HashSet<>();

    protected ConfigurationNode configBurnerBlock = new ConfigurationNode("Block", "Burner");
    protected ConfigurationNode.KnownParser.SingleKnown<Double> configSpecialBlockPercent = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_DOUBLE, "Block", "Special", "Percent");
    protected ConfigurationNode.KnownParser.CollectionKnown<BlockType, Collection<BlockType>> configSpecialBlockType = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_BLOCK_TYPE, "Block", "Special", "Type");
    protected ConfigurationNode.KnownParser.SingleKnown<Integer> configFuelConsumption = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Block", "Fuel", "Consumption");
    protected ConfigurationNode.KnownParser.SingleKnown<FuelSlot> configFuelSlot = new ConfigurationNode.KnownParser.SingleKnown<>(new StringToEnumParser<>(FuelSlot.class), "Block", "Fuel", "Slot");
    protected ConfigurationNode.KnownParser.CollectionKnown<ItemType, Set<ItemType>> configFuelTypes = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_ITEM_TYPE, "Block", "Fuel", "Types");

    public Submarine(SubmarineType type, LiveSignTileEntity licence) throws NoLicencePresent {
        super(licence, type);
    }

    public Submarine(SubmarineType type, SignTileEntity ste, SyncBlockPosition position) {
        super(ste, position, type);
    }

    public float getSpecialBlockPercent() {
        if (this.specialBlockPercent == null) {
            return this.getType().getDefaultSpecialBlockPercent();
        }
        return this.specialBlockPercent;
    }

    public int getFuelConsumption() {
        if (this.fuelConsumption == null) {
            return this.getType().getDefaultFuelConsumption();
        }
        return this.fuelConsumption;
    }

    public FuelSlot getFuelSlot() {
        if (this.fuelSlot == null) {
            return this.getType().getDefaultFuelSlot();
        }
        return this.fuelSlot;
    }

    public @NotNull Set<BlockType> getSpecialBlocks() {
        if (this.specialBlocks.isEmpty()) {
            return this.getType().getDefaultSpecialBlockType();
        }
        return this.specialBlocks;
    }

    public @NotNull Set<ItemType> getFuelTypes() {
        if (this.fuelTypes.isEmpty()) {
            return this.getType().getDefaultFuelTypes();
        }
        return this.fuelTypes;
    }

    @Override
    public @NotNull SubmarineType getType() {
        return (SubmarineType) super.getType();
    }

    @Override
    public @NotNull Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Fuel", ArrayUtils.toString(", ", Parser.STRING_TO_ITEM_TYPE::unparse, this.getFuelTypes()));
        map.put("Fuel Consumption", this.getFuelConsumption() + "");
        map.put("Fuel Slot", (this.getFuelSlot().name()));
        map.put("Special Block", ArrayUtils.toString(", ", Parser.STRING_TO_BLOCK_TYPE::unparse, this.getSpecialBlocks()));
        map.put("Required Percent", this.getSpecialBlockPercent() + "");
        return map;
    }

    @Override
    public Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(ConfigurationStream file) {
        Map<ConfigurationNode.KnownParser<?, ?>, Object> map = new HashMap<>();
        map.put(this.configSpecialBlockType, this.getSpecialBlocks());
        map.put(this.configSpecialBlockPercent, this.getSpecialBlockPercent());
        map.put(this.configFuelConsumption, this.getFuelConsumption());
        map.put(this.configFuelSlot, this.getFuelSlot());
        map.put(this.configFuelTypes, this.getFuelTypes());
        return map;
    }

    @Override
    public AbstractShipsVessel deserializeExtra(ConfigurationStream file) {
        file.getDouble(this.configSpecialBlockPercent).ifPresent(c -> this.specialBlockPercent = c.floatValue());
        file.getInteger(this.configFuelConsumption).ifPresent(c -> this.fuelConsumption = c);
        this.fuelTypes = file.parseCollection(this.configFuelTypes, new HashSet<>());
        this.specialBlocks = file.parseCollection(this.configSpecialBlockType, new HashSet<>());
        this.fuelSlot = file.parse(this.configFuelSlot).orElse(FuelSlot.BOTTOM);
        return this;
    }

    @Override
    public void meetsRequirements(MovementContext context) throws MoveException {
        VesselRequirement.super.meetsRequirements(context);
        if (!context.isStrictMovement()) {
            return;
        }
        Optional<Integer> opWaterLevel = getWaterLevel(MovingBlock::getAfterPosition, context.getMovingStructure());
        if (!opWaterLevel.isPresent()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NO_MOVING_TO_FOUND, Collections.singletonList(BlockTypes.WATER)));
        }
        int specialBlocks = 0;
        Set<FurnaceInventory> furnaceInventories = new HashSet<>();
        for (MovingBlock block : context.getMovingStructure()) {
            BlockDetails details = block.getStoredBlockData();
            if (this.getSpecialBlocks().stream().anyMatch(b -> b.equals(details.getType()))) {
                specialBlocks++;
            }
            Optional<TileEntitySnapshot<? extends TileEntity>> opTile = details.get(KeyedData.TILED_ENTITY);
            if (!opTile.isPresent()) {
                continue;
            }
            if (!(opTile.get() instanceof FurnaceTileEntity)) {
                continue;
            }
            furnaceInventories.add(((FurnaceTileEntity) opTile.get()).getInventory());
        }
        float specialBlockPercent = ((specialBlocks * 100.0f) / context.getMovingStructure().stream().filter(m -> !m.getStoredBlockData().getType().equals(BlockTypes.AIR)).count());
        if ((this.getSpecialBlockPercent() != 0) && specialBlockPercent <= this.getSpecialBlockPercent()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_PERCENT, new RequiredPercentMovementData(this.getSpecialBlocks().iterator().next(), this.getSpecialBlockPercent(), specialBlockPercent)));
        }
        if (this.getFuelConsumption() != 0 && (!this.getFuelTypes().isEmpty())) {
            List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.BOTTOM) ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().isPresent();
            }).filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.BOTTOM) ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().map(ItemStack::getQuantity).orElse(0) >= this.getFuelConsumption();
            }).filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.BOTTOM) ? i.getSmeltingSlot() : i.getFuelSlot();
                return this.getFuelTypes().stream().anyMatch(type -> slot.getItem().map(item -> item.getType().equals(type)).orElse(false));
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
        Optional<Integer> opWaterLevel = getWaterLevel(MovingBlock::getAfterPosition, context.getMovingStructure());
        if (!opWaterLevel.isPresent()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NO_MOVING_TO_FOUND, Collections.singletonList(BlockTypes.WATER)));
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
        if (this.getFuelConsumption() != 0 && (!this.getFuelTypes().isEmpty())) {
            List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.BOTTOM) ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().isPresent();
            }).filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.BOTTOM) ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().map(ItemStack::getQuantity).orElse(0) >= this.getFuelConsumption();
            }).filter(i -> {
                Slot slot = this.getFuelSlot().equals(FuelSlot.BOTTOM) ? i.getSmeltingSlot() : i.getFuelSlot();
                return this.getFuelTypes().stream().anyMatch(type -> slot.getItem().map(item -> item.getType().equals(type)).orElse(false));
            }).collect(Collectors.toList());
            if (acceptedSlots.isEmpty()) {
                throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_FUEL, new RequiredFuelMovementData(this.getFuelConsumption(), this.getFuelTypes())));
            }
            FurnaceInventory inv = acceptedSlots.get(0);
            Slot slot = this.getFuelSlot().equals(FuelSlot.BOTTOM) ? inv.getSmeltingSlot() : inv.getFuelSlot();
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
    public void setStructure(@NotNull PositionableShipsStructure structure) {
        structure.addAir();
        super.setStructure(structure);
    }
}
