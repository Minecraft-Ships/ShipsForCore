package org.ships.vessel.common.types.typical.submarine;

import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.inventory.inventories.general.block.FurnaceInventory;
import org.core.inventory.item.ItemStack;
import org.core.inventory.item.ItemType;
import org.core.inventory.item.ItemTypes;
import org.core.inventory.parts.Slot;
import org.core.world.position.BlockPosition;
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
import org.ships.exceptions.MoveException;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.movement.result.data.RequiredFuelMovementData;
import org.ships.movement.result.data.RequiredPercentMovementData;
import org.ships.vessel.common.assits.UnderWaterType;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;
import java.util.stream.Collectors;

public class Submarine extends AbstractShipsVessel implements UnderWaterType {

    protected float specialBlockPercent = 75;
    protected Set<BlockType> specialBlocks = new HashSet<>(Collections.singletonList(BlockTypes.IRON_BLOCK.get()));
    protected int fuelConsumption = 1;
    protected boolean takeFromTopSlot = false;
    protected Set<ItemType> fuelTypes = new HashSet<>(Collections.singletonList(ItemTypes.COAL_BLOCK));

    public Submarine(LiveSignTileEntity licence) {
        super(licence, ShipType.SUBMARINE);
    }

    public Submarine(SignTileEntity ste, BlockPosition position) {
        super(ste, position, ShipType.SUBMARINE);
    }

    public float getSpecialBlockPercent(){
        return this.specialBlockPercent;
    }

    public int getFuelConsumption(){
        return this.fuelConsumption;
    }

    public boolean shouldTakeFromTopSlot(){
        return this.takeFromTopSlot;
    }

    public Set<BlockType> getSpecialBlocks(){
        return this.specialBlocks;
    }

    public Set<ItemType> getFuelTypes(){
        return this.fuelTypes;
    }

    @Override
    public Map<String, String> getExtraInformation() {
        return new HashMap<>();
    }

    @Override
    public Map<ConfigurationNode, Object> serialize(ConfigurationFile file) {
        return new HashMap<>();
    }

    @Override
    public AbstractShipsVessel deserializeExtra(ConfigurationFile file) {
        return this;
    }

    @Override
    public void meetsRequirements(boolean strict, MovingBlockSet movingBlocks) throws MoveException {
        if(!strict){
            return;
        }
        int specialBlocks = 0;
        Set<FurnaceInventory> furnaceInventories = new HashSet<>();
        for(MovingBlock block : movingBlocks){
            BlockDetails details = block.getStoredBlockData();
            if(this.specialBlocks.stream().anyMatch(b -> b.equals(details.getType()))){
                specialBlocks++;
            }
            Optional<TileEntitySnapshot<? extends TileEntity>> opTile = details.get(KeyedData.TILED_ENTITY);
            if(!opTile.isPresent()){
                continue;
            }
            if(!(opTile.get() instanceof FurnaceTileEntity)){
                continue;
            }
            furnaceInventories.add(((FurnaceTileEntity)opTile.get()).getInventory());
        }
        float specialBlockPercent = ((specialBlocks * 100.0f)/movingBlocks.stream().filter(m -> !m.getStoredBlockData().getType().equals(BlockTypes.AIR.get())).count());
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
    public void processRequirements(boolean strict, MovingBlockSet movingBlocks) throws MoveException {
        if(!strict){
            return;
        }
        Set<FurnaceInventory> furnaceInventories = new HashSet<>();
        for(MovingBlock movingBlock : movingBlocks){
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
    public void setStructure(PositionableShipsStructure structure){
        structure.addAir();
        super.setStructure(structure);
    }
}
