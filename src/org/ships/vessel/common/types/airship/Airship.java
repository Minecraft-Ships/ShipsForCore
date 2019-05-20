package org.ships.vessel.common.types.airship;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.inventory.inventories.general.block.FurnaceInventory;
import org.core.inventory.item.ItemStack;
import org.core.inventory.item.ItemType;
import org.core.inventory.item.ItemTypes;
import org.core.inventory.parts.Slot;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.blocks.furnace.GeneralFurnace;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.movement.result.data.RequiredFuelMovementData;
import org.ships.movement.result.data.RequiredPercentMovementData;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.types.AbstractShipsVessel;
import org.ships.vessel.common.types.ShipType;

import java.util.*;
import java.util.stream.Collectors;

public class Airship extends AbstractShipsVessel implements AirType {

    protected boolean useBurner = true;
    protected float specialBlockPercent = 60;
    protected Set<BlockType> specialBlocks = BlockTypes.WHITE_WOOL.get().getLike();
    protected int fuelConsumption = 1;
    protected boolean takeFromTopSlot = false;
    protected Set<ItemType> fuelTypes = new HashSet<>(Arrays.asList(ItemTypes.COAL, ItemTypes.CHARCOAL));

    protected ConfigurationNode configBurnerBlock = new ConfigurationNode("Block", "Burner");
    protected ConfigurationNode configSpecialBlockPercent = new ConfigurationNode("Block", "Special", "Percent");
    protected ConfigurationNode configSpecialBlockType = new ConfigurationNode("Block", "Special", "Type");
    protected ConfigurationNode configFuelConsumption = new ConfigurationNode("Block", "Fuel", "Consumption");
    protected ConfigurationNode configFuelSlot = new ConfigurationNode("Block", "Fuel", "Slot");
    protected ConfigurationNode configFuelTypes = new ConfigurationNode("Block", "Fuel", "Types");

    public Airship(LiveSignTileEntity licence) {
        super(licence, ShipType.WATERSHIP);
    }

    public Airship(SignTileEntity ste, BlockPosition position){
        super(ste, position, ShipType.WATERSHIP);
    }

    @Override
    public void meetsRequirement(MovingBlockSet movingBlocks) throws MoveException{
        int specialBlockCount = 0;
        boolean burnerFound = false;
        Set<FurnaceInventory> furnaceInventories = new HashSet<>();
        for(MovingBlock movingBlock : movingBlocks){
            BlockPosition blockPosition = movingBlock.getBeforePosition();
            BlockDetails details = movingBlock.getStoredBlockData();
            if(blockPosition.getBlockType().equals(BlockTypes.FIRE)){
                burnerFound = true;
            }
            if(this.specialBlocks.stream().anyMatch(b -> b.equals(blockPosition.getBlockType()))){
                specialBlockCount++;
            }
            if(details instanceof GeneralFurnace){
                GeneralFurnace furnaceDetails = (GeneralFurnace) details;
                furnaceInventories.add(furnaceDetails.getTileEntity().getInventory());
            }
        }
        if(this.useBurner && !burnerFound){
            throw new MoveException(new AbstractFailedMovement(this, MovementResult.NO_BURNER_FOUND, false));
        }
        float specialBlockPercent = ((specialBlockCount * 100.0f)/movingBlocks.size());
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
    public ShipType getType() {
        return ShipType.AIRSHIP;
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
}
