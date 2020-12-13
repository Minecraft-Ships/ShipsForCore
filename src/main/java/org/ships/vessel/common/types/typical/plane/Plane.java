package org.ships.vessel.common.types.typical.plane;

import org.array.utils.ArrayUtils;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.config.parser.parsers.StringToEnumParser;
import org.core.inventory.inventories.general.block.FurnaceInventory;
import org.core.inventory.item.ItemType;
import org.core.inventory.item.stack.ItemStack;
import org.core.inventory.parts.Slot;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.BlockSnapshot;
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
import org.ships.movement.autopilot.FlightPath;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.movement.result.data.RequiredFuelMovementData;
import org.ships.vessel.common.assits.*;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;
import java.util.stream.Collectors;

public class Plane extends AbstractShipsVessel implements AirType, VesselRequirement, Fallable, FlightPathType {

    protected Integer fuelConsumption;
    protected FuelSlot fuelSlot;
    protected Set<ItemType> fuelTypes = new HashSet<>();
    protected FlightPath path;

    protected ConfigurationNode.KnownParser.SingleKnown<Integer> configFuelConsumption = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Block", "Fuel", "Consumption");
    protected ConfigurationNode.KnownParser.SingleKnown<FuelSlot> configFuelSlot = new ConfigurationNode.KnownParser.SingleKnown<>(new StringToEnumParser<>(FuelSlot.class), "Block", "Fuel", "Slot");
    protected ConfigurationNode.KnownParser.CollectionKnown<ItemType, Set<ItemType>> configFuelTypes = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_ITEM_TYPE, "Block", "Fuel", "Types");

    public Plane(LiveSignTileEntity licence, ShipType<Plane> type) {
        super(licence, type);
    }

    public Plane(SignTileEntity ste, SyncBlockPosition position, ShipType<Plane> type) {
        super(ste, position, type);
    }

    public Set<ItemType> getFuelTypes(){
        if(this.fuelTypes.isEmpty()){
            return this.getType().getDefaultFuelTypes();
        }
        return this.fuelTypes;
    }

    public int getFuelConsumption(){
        if(this.fuelConsumption != null){
            return this.fuelConsumption;
        }
        return this.getType().getDefaultFuelConsumption();
    }

    public FuelSlot getFuelSlot(){
        if(this.fuelSlot == null){
            return this.getType().getDefaultFuelSlot();
        }
        return this.fuelSlot;
    }

    @Override
    public PlaneType getType() {
        return (PlaneType)super.getType();
    }

    @Override
    public Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Fuel", ArrayUtils.toString(", ", Parser.STRING_TO_ITEM_TYPE::unparse, this.getFuelTypes()));
        map.put("Fuel Consumption", this.getFuelConsumption() + "");
        map.put("Fuel Slot", this.fuelSlot.name());
        return map;
    }

    @Override
    public Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(ConfigurationStream file) {
        Map<ConfigurationNode.KnownParser<?, ?>, Object> map = new HashMap<>();
        map.put(this.configFuelConsumption, this.getFuelConsumption());
        map.put(this.configFuelSlot, this.fuelSlot);
        map.put(this.configFuelTypes, this.getFuelTypes());
        return map;
    }

    @Override
    public AbstractShipsVessel deserializeExtra(ConfigurationStream file) {
        file.getInteger(this.configFuelConsumption).ifPresent(v -> this.fuelConsumption = v);
        this.fuelTypes = file.parseCollection(this.configFuelTypes, new HashSet<>());
        this.fuelSlot = file.parse(this.configFuelSlot).orElse(null);
        return this;
    }

    @Override
    public void meetsRequirements(MovementContext context) throws MoveException {
        if (!context.isStrictMovement()) {
            return;
        }
        if (this.getFuelConsumption() == 0 || (this.getFuelTypes().isEmpty())) {
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
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_FUEL, new RequiredFuelMovementData(this.fuelConsumption, this.fuelTypes)));
        }
    }

    @Override
    public void processRequirements(MovementContext context) throws MoveException {
        if (!context.isStrictMovement()) {
            return;
        }
        if (this.getFuelConsumption() == 0 || (this.getFuelTypes().isEmpty())) {
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
        List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
            Slot slot = this.getFuelSlot().equals(FuelSlot.TOP) ? i.getSmeltingSlot() : i.getFuelSlot();
            return slot.getItem().isPresent();
        }).filter(i -> {
            Slot slot = this.getFuelSlot().equals(FuelSlot.TOP) ? i.getSmeltingSlot() : i.getFuelSlot();
            return slot.getItem().get().getQuantity() >= this.fuelConsumption;
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

    @Override
    public boolean shouldFall() {
        if(this.getFuelConsumption() == 0 || this.getFuelTypes().isEmpty()){
            return false;
        }
        Set<FurnaceInventory> furnaceInventories = new HashSet<>();
        for (SyncBlockPosition loc : this.getStructure().getPositions()) {
            BlockSnapshot<SyncBlockPosition> snapshot = loc.getBlockDetails();
            Optional<TileEntitySnapshot<? extends TileEntity>> opTiled = snapshot.get(KeyedData.TILED_ENTITY);
            if (opTiled.isPresent()) {
                if (opTiled.get() instanceof FurnaceTileEntitySnapshot) {
                    furnaceInventories.add(((FurnaceTileEntitySnapshot) opTiled.get()).getInventory());
                }
            }
        }
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
        return acceptedSlots.isEmpty();
    }

    @Override
    public Optional<FlightPath> getFlightPath() {
        return Optional.ofNullable(this.path);
    }

    @Override
    public FlightPathType setFlightPath(FlightPath path) {
        this.path = path;
        return this;
    }
}
