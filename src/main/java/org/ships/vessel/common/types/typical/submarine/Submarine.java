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
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.UnderWaterType;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.requirement.FuelRequirement;
import org.ships.vessel.common.requirement.Requirement;
import org.ships.vessel.common.requirement.SpecialBlocksRequirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;

public class Submarine extends AbstractShipsVessel implements UnderWaterType, VesselRequirement {

    protected final ConfigurationNode configBurnerBlock = new ConfigurationNode("Block", "Burner");
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
    protected @Deprecated
    @Nullable Float specialBlockPercent;
    protected @Deprecated
    @Nullable Collection<BlockType> specialBlocks;
    protected @Deprecated
    @Nullable Integer fuelConsumption;
    protected @Deprecated
    @Nullable FuelSlot fuelSlot;
    protected @Deprecated
    @Nullable Collection<ItemType> fuelTypes;

    private Collection<Requirement> requirements = new HashSet<>();

    public Submarine(ShipType<? extends Submarine> type, LiveTileEntity licence) throws NoLicencePresent {
        super(licence, type);
        this.initRequirements();
    }

    public Submarine(ShipType<? extends Submarine> type, SignTileEntity ste, SyncBlockPosition position) {
        super(ste, position, type);
        this.initRequirements();
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

    @Deprecated(forRemoval = true)
    public float getSpecialBlockPercent() {
        return this.getSpecialBlocksPercent();
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
    public @NotNull Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Fuel", ArrayUtils.toString(", ", Parser.STRING_TO_ITEM_TYPE::unparse, this.getFuelTypes()));
        map.put("Fuel Consumption", this.getFuelConsumption() + "");
        map.put("Fuel Slot", (this.getFuelSlot().name()));
        map.put("Special Block",
                ArrayUtils.toString(", ", Parser.STRING_TO_BLOCK_TYPE::unparse, this.getSpecialBlocks()));
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
    public Collection<Requirement> getRequirements() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void meetsRequirements(MovementContext context) throws MoveException {
        VesselRequirement.super.meetsRequirements(context);
        if (!context.isStrictMovement()) {
            return;
        }
        Optional<Integer> opWaterLevel = this.getWaterLevel(MovingBlock::getAfterPosition,
                context.getMovingStructure());
        if (opWaterLevel.isEmpty()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NO_MOVING_TO_FOUND,
                    Collections.singletonList(BlockTypes.WATER)));
        }
        int specialBlocks = 0;
        Collection<FurnaceInventory> furnaceInventories = new HashSet<>();
        for (MovingBlock block : context.getMovingStructure()) {
            BlockDetails details = block.getStoredBlockData();
            if (this.getSpecialBlocks().stream().anyMatch(b -> b.equals(details.getType()))) {
                specialBlocks++;
            }
            Optional<TileEntitySnapshot<? extends TileEntity>> opTile = details.get(KeyedData.TILED_ENTITY);
            if (opTile.isEmpty()) {
                continue;
            }
            if (!(opTile.get() instanceof FurnaceTileEntity)) {
                continue;
            }
            furnaceInventories.add(((FurnaceTileEntity) opTile.get()).getInventory());
        }
        float specialBlockPercent = ((specialBlocks * 100.0f) / context
                .getMovingStructure()
                .stream()
                .filter(m -> !m.getStoredBlockData().getType().equals(BlockTypes.AIR))
                .count());
        if ((this.getSpecialBlockPercent() != 0) && specialBlockPercent <= this.getSpecialBlockPercent()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_PERCENT,
                    new RequiredPercentMovementData(this.getSpecialBlocks().iterator().next(),
                            this.getSpecialBlockPercent(), specialBlockPercent)));
        }
        if (this.getFuelConsumption() != 0 && (!this.getFuelTypes().isEmpty())) {
            List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
                Slot slot = this.getFuelSlot() == FuelSlot.BOTTOM ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().isPresent();
            }).filter(i -> {
                Slot slot = this.getFuelSlot() == FuelSlot.BOTTOM ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().map(ItemStack::getQuantity).orElse(0) >= this.getFuelConsumption();
            }).filter(i -> {
                Slot slot = this.getFuelSlot() == FuelSlot.BOTTOM ? i.getSmeltingSlot() : i.getFuelSlot();
                return this
                        .getFuelTypes()
                        .stream()
                        .anyMatch(type -> slot.getItem().map(item -> item.getType().equals(type)).orElse(false));
            }).toList();
            if (acceptedSlots.isEmpty()) {
                throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_FUEL,
                        new RequiredFuelMovementData(this.getFuelConsumption(), this.getFuelTypes())));
            }
        }
    }

    @Override
    public void processRequirements(MovementContext context) throws MoveException {
        VesselRequirement.super.processRequirements(context);
        if (!context.isStrictMovement()) {
            return;
        }
        Optional<Integer> opWaterLevel = this.getWaterLevel(MovingBlock::getAfterPosition,
                context.getMovingStructure());
        if (opWaterLevel.isEmpty()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NO_MOVING_TO_FOUND,
                    Collections.singletonList(BlockTypes.WATER)));
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
        if (this.getFuelConsumption() != 0 && (!this.getFuelTypes().isEmpty())) {
            List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
                Slot slot = this.getFuelSlot() == FuelSlot.BOTTOM ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().isPresent();
            }).filter(i -> {
                Slot slot = this.getFuelSlot() == FuelSlot.BOTTOM ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().map(ItemStack::getQuantity).orElse(0) >= this.getFuelConsumption();
            }).filter(i -> {
                Slot slot = this.getFuelSlot() == FuelSlot.BOTTOM ? i.getSmeltingSlot() : i.getFuelSlot();
                return this
                        .getFuelTypes()
                        .stream()
                        .anyMatch(type -> slot.getItem().map(item -> item.getType().equals(type)).orElse(false));
            }).toList();
            if (acceptedSlots.isEmpty()) {
                throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_FUEL,
                        new RequiredFuelMovementData(this.getFuelConsumption(), this.getFuelTypes())));
            }
            FurnaceInventory inv = acceptedSlots.get(0);
            Slot slot = this.getFuelSlot() == FuelSlot.BOTTOM ? inv.getSmeltingSlot() : inv.getFuelSlot();
            Optional<ItemStack> opItem = slot.getItem();
            if (opItem.isEmpty()) {
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
        this.getRequirement(updated.getClass()).ifPresent(req -> this.requirements.remove(req));
        this.requirements.add(updated);
    }
}
