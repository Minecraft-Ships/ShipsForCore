package org.ships.vessel.common.types.typical.airship;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.inventory.inventories.general.block.FurnaceInventory;
import org.core.inventory.item.ItemStack;
import org.core.inventory.item.ItemType;
import org.core.inventory.parts.Slot;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.block.entity.TileEntity;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.container.furnace.FurnaceTileEntitySnapshot;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlock;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.movement.result.data.RequiredFuelMovementData;
import org.ships.movement.result.data.RequiredPercentMovementData;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.assits.Fallable;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;
import java.util.stream.Collectors;

public class Airship extends AbstractShipsVessel implements AirType, Fallable, org.ships.vessel.common.assits.VesselRequirement {

    protected boolean useBurner = ShipType.AIRSHIP.isUsingBurner();
    protected float specialBlockPercent = ShipType.AIRSHIP.getDefaultSpecialBlockPercent();
    protected Set<BlockType> specialBlocks = ShipType.AIRSHIP.getDefaultSpecialBlockType();
    protected int fuelConsumption = ShipType.AIRSHIP.getDefaultFuelConsumption();
    protected boolean takeFromTopSlot = ShipType.AIRSHIP.isUsingTopSlot();
    protected Set<ItemType> fuelTypes = ShipType.AIRSHIP.getDefaultFuelTypes();

    protected ConfigurationNode configBurnerBlock = new ConfigurationNode("Block", "Burner");
    protected ConfigurationNode configSpecialBlockPercent = new ConfigurationNode("Block", "Special", "Percent");
    protected ConfigurationNode configSpecialBlockType = new ConfigurationNode("Block", "Special", "Type");
    protected ConfigurationNode configFuelConsumption = new ConfigurationNode("Block", "Fuel", "Consumption");
    protected ConfigurationNode configFuelSlot = new ConfigurationNode("Block", "Fuel", "Slot");
    protected ConfigurationNode configFuelTypes = new ConfigurationNode("Block", "Fuel", "Types");

    public Airship(AirshipType type, LiveSignTileEntity licence) {
        super(licence, type);
    }

    public Airship(AirshipType type, SignTileEntity ste, BlockPosition position){
        super(ste, position, type);
    }

    public float getSpecialBlockPercent(){
        return this.specialBlockPercent;
    }

    public Airship setSpecialBlockPercent(float percent){
        this.specialBlockPercent = percent;
        return this;
    }

    public int getFuelConsumption(){
        return this.fuelConsumption;
    }

    public Airship setFuelConsumption(int fuel){
        this.fuelConsumption = fuel;
        return this;
    }

    public boolean shouldTakeFromTopSlot(){
        return this.takeFromTopSlot;
    }

    public Airship setShouldTakeFromTopSlot(boolean check){
        this.takeFromTopSlot = check;
        return this;
    }

    public Set<BlockType> getSpecialBlocks(){
        return this.specialBlocks;
    }

    public Set<ItemType> getFuelTypes(){
        return this.fuelTypes;
    }

    @Override
    public void meetsRequirements(MovementContext context) throws MoveException{
        if(!context.isStrictMovement()){
            return;
        }
        int specialBlockCount = 0;
        boolean burnerFound = false;
        Set<FurnaceInventory> furnaceInventories = new HashSet<>();
        for(MovingBlock movingBlock : context.getMovingStructure()){
            if(movingBlock.getBeforePosition().isPresent()) {
                BlockPosition blockPosition = movingBlock.getBeforePosition().get();
                BlockDetails details = movingBlock.getStoredBlockData();
                if (blockPosition.getBlockType().equals(BlockTypes.FIRE.get())) {
                    burnerFound = true;
                }
                if (this.specialBlocks.stream().anyMatch(b -> b.equals(blockPosition.getBlockType()))) {
                    specialBlockCount++;
                }
                Optional<TileEntitySnapshot<? extends TileEntity>> opTiled = details.get(KeyedData.TILED_ENTITY);
                if (opTiled.isPresent()) {
                    if (opTiled.get() instanceof FurnaceTileEntitySnapshot) {
                        furnaceInventories.add(((FurnaceTileEntitySnapshot) opTiled.get()).getInventory());
                    }
                }
            }
        }
        if(this.useBurner && !burnerFound){
            throw new MoveException(new AbstractFailedMovement(this, MovementResult.NO_BURNER_FOUND, false));
        }
        float specialBlockPercent = ((specialBlockCount * 100.0f)/context.getMovingStructure().size());
        if((this.specialBlockPercent != 0) && specialBlockPercent <= this.specialBlockPercent){
            throw new MoveException(new AbstractFailedMovement(this, MovementResult.NOT_ENOUGH_PERCENT, new RequiredPercentMovementData(this.specialBlocks.iterator().next(), this.specialBlockPercent, specialBlockPercent)));
        }
        if(this.fuelConsumption != 0 && (!this.fuelTypes.isEmpty())){
            List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
                Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().isPresent();
            }).filter(i -> {
                Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().get().getQuantity() >= this.fuelConsumption;
            }).filter(i -> {
                Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
                return this.fuelTypes.stream().anyMatch(type -> slot.getItem().get().getType().equals(type));
            }).collect(Collectors.toList());
            if(acceptedSlots.isEmpty()){
                throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_FUEL, new RequiredFuelMovementData(this.fuelConsumption, this.fuelTypes)));
            }
        }
    }

    @Override
    public void processRequirements(MovementContext context) throws MoveException {
        if(!context.isStrictMovement()){
            return;
        }
        Set<FurnaceInventory> furnaceInventories = new HashSet<>();
        for(MovingBlock movingBlock : context.getMovingStructure()){
            BlockDetails details = movingBlock.getStoredBlockData();
            Optional<TileEntitySnapshot<? extends TileEntity>> opTiled = details.get(KeyedData.TILED_ENTITY);
            if(opTiled.isPresent()){
                if(opTiled.get() instanceof FurnaceTileEntitySnapshot){
                    furnaceInventories.add(((FurnaceTileEntitySnapshot) opTiled.get()).getInventory());
                }
            }
        }
        if(this.fuelConsumption != 0 && (!this.fuelTypes.isEmpty())){
            List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
                Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().isPresent();
            }).filter(i -> {
                Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().get().getQuantity() >= this.fuelConsumption;
            }).filter(i -> {
                Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
                return this.fuelTypes.stream().anyMatch(type -> slot.getItem().get().getType().equals(type));
            }).collect(Collectors.toList());
            if(acceptedSlots.isEmpty()){
                throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_FUEL, new RequiredFuelMovementData(this.fuelConsumption, this.fuelTypes)));
            }
            FurnaceInventory inv = acceptedSlots.get(0);
            Slot slot = this.takeFromTopSlot ? inv.getSmeltingSlot() : inv.getFuelSlot();
            ItemStack item = slot.getItem().get();
            item = item.copyWithQuantity(item.getQuantity() - this.fuelConsumption);
            if(item.getQuantity() == 0){
                item = null;
            }
            slot.setItem(item);
        }
    }

    @Override
    public Map<ConfigurationNode, Object> serialize(ConfigurationFile file) {
        Map<ConfigurationNode, Object> map = new HashMap<>();
        map.put(this.configBurnerBlock, this.useBurner);
        map.put(this.configSpecialBlockType, Parser.unparseList(Parser.STRING_TO_BLOCK_TYPE, this.specialBlocks));
        map.put(this.configSpecialBlockPercent, this.specialBlockPercent);
        map.put(this.configFuelConsumption, this.fuelConsumption);
        map.put(this.configFuelSlot, this.takeFromTopSlot ? "Top" : "Bottom");
        map.put(this.configFuelTypes, Parser.unparseList(Parser.STRING_TO_ITEM_TYPE, this.fuelTypes));
        return map;
    }

    @Override
    public AbstractShipsVessel deserializeExtra(ConfigurationFile file) {
        this.useBurner = file.parseBoolean(this.configBurnerBlock).get();
        this.specialBlockPercent = file.parseDouble(this.configSpecialBlockPercent).get().floatValue();
        this.fuelConsumption = file.parseInt(this.configFuelConsumption).get();
        Optional<List<ItemType>> opItemTypes = file.parseList(this.configFuelTypes, Parser.STRING_TO_ITEM_TYPE);
        this.fuelTypes = opItemTypes.<Set<ItemType>>map(HashSet::new).orElseGet(HashSet::new);
        Optional<List<BlockType>> opSpecialBlocks = file.parseList(this.configSpecialBlockType, Parser.STRING_TO_BLOCK_TYPE);
        this.specialBlocks = opSpecialBlocks.<Set<BlockType>>map(HashSet::new).orElseGet(HashSet::new);
        String slotType = file.parseString(this.configFuelSlot).get().toLowerCase();
        switch(slotType){
            case "top": this.takeFromTopSlot = true; break;
            case "bottom": this.takeFromTopSlot = false; break;
            default: System.err.println("Failed to read " + this.configFuelSlot.toString() + ". Only 'Top' and 'Bottom' are allowed as values. Using default"); break;
        }
        return this;
    }

    @Override
    public Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Fuel", CorePlugin.toString(", ", Parser.STRING_TO_ITEM_TYPE::unparse, this.fuelTypes));
        map.put("Fuel Consumption", this.fuelConsumption + "");
        map.put("Fuel Slot", (this.takeFromTopSlot ? "True" : "False"));
        map.put("Special Block", CorePlugin.toString(", ", Parser.STRING_TO_BLOCK_TYPE::unparse, this.specialBlocks));
        map.put("Required Percent", this.specialBlockPercent + "");
        map.put("Requires Burner", this.useBurner + "");
        return map;
    }

    @Override
    public boolean shouldFall() {
        int specialBlockCount = 0;
        boolean burnerFound = false;
        Set<FurnaceInventory> furnaceInventories = new HashSet<>();
        for(BlockPosition position : this.getStructure().getPositions()){
            if(position.getBlockType().equals(BlockTypes.FIRE.get())){
                burnerFound = true;
            }
            if(this.specialBlocks.stream().anyMatch(b -> b.equals(position.getBlockType()))){
                specialBlockCount++;
            }
            Optional<TileEntitySnapshot<? extends TileEntity>> opTiled = position.getBlockDetails().get(KeyedData.TILED_ENTITY);
            if(opTiled.isPresent()){
                if(opTiled.get() instanceof FurnaceTileEntitySnapshot){
                    furnaceInventories.add(((FurnaceTileEntitySnapshot) opTiled.get()).getInventory());
                }
            }
        }
        if(this.useBurner && !burnerFound){
            return false;
        }
        float specialBlockPercent = ((specialBlockCount * 100.0f)/getStructure().getPositions().size());
        if((this.specialBlockPercent != 0) && specialBlockPercent <= this.specialBlockPercent){
            return false;
        }
        if(this.fuelConsumption != 0 && (!this.fuelTypes.isEmpty())){
            List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
                Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().isPresent();
            }).filter(i -> {
                Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
                return slot.getItem().get().getQuantity() >= this.fuelConsumption;
            }).filter(i -> {
                Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
                return this.fuelTypes.stream().anyMatch(type -> slot.getItem().get().getType().equals(type));
            }).collect(Collectors.toList());
            return !acceptedSlots.isEmpty();
        }
        return true;
    }
}
