package org.ships.vessel.common.types.typical.plane;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.inventory.inventories.general.block.FurnaceInventory;
import org.core.inventory.item.ItemType;
import org.core.inventory.item.stack.ItemStack;
import org.core.inventory.parts.Slot;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.BlockSnapshot;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.block.entity.TileEntity;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.container.furnace.FurnaceTileEntitySnapshot;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlock;
import org.ships.movement.autopilot.FlightPath;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.movement.result.data.RequiredFuelMovementData;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.assits.Fallable;
import org.ships.vessel.common.assits.FlightPathType;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;
import java.util.stream.Collectors;

public class Plane extends AbstractShipsVessel implements AirType, VesselRequirement, Fallable, FlightPathType {

    protected int fuelConsumption = ShipType.PLANE.getDefaultFuelConsumption();
    protected boolean takeFromTopSlot = ShipType.PLANE.isUsingTopSlot();
    protected Set<ItemType> fuelTypes = ShipType.PLANE.getDefaultFuelTypes();
    protected FlightPath path;

    protected ConfigurationNode configFuelConsumption = new ConfigurationNode("Block", "Fuel", "Consumption");
    protected ConfigurationNode configFuelSlot = new ConfigurationNode("Block", "Fuel", "Slot");
    protected ConfigurationNode configFuelTypes = new ConfigurationNode("Block", "Fuel", "Types");

    public Plane(LiveSignTileEntity licence, ShipType type) {
        super(licence, type);
    }

    public Plane(SignTileEntity ste, BlockPosition position, ShipType type) {
        super(ste, position, type);
    }

    @Override
    public Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Fuel", CorePlugin.toString(", ", Parser.STRING_TO_ITEM_TYPE::unparse, this.fuelTypes));
        map.put("Fuel Consumption", this.fuelConsumption + "");
        map.put("Fuel Slot", (this.takeFromTopSlot ? "True" : "False"));
        return map;
    }

    @Override
    public Map<ConfigurationNode, Object> serialize(ConfigurationFile file) {
        Map<ConfigurationNode, Object> map = new HashMap<>();
        map.put(this.configFuelConsumption, this.fuelConsumption);
        map.put(this.configFuelSlot, this.takeFromTopSlot ? "Top" : "Bottom");
        map.put(this.configFuelTypes, Parser.unparseList(Parser.STRING_TO_ITEM_TYPE, this.fuelTypes));
        return map;
    }

    @Override
    public AbstractShipsVessel deserializeExtra(ConfigurationFile file) {
        this.fuelConsumption = file.parseInt(this.configFuelConsumption).get();
        Optional<List<ItemType>> opItemTypes = file.parseList(this.configFuelTypes, Parser.STRING_TO_ITEM_TYPE);
        this.fuelTypes = opItemTypes.<Set<ItemType>>map(HashSet::new).orElseGet(HashSet::new);
        String slotType = file.parseString(this.configFuelSlot).get().toLowerCase();
        switch (slotType) {
            case "top":
                this.takeFromTopSlot = true;
                break;
            case "bottom":
                this.takeFromTopSlot = false;
                break;
            default:
                System.err.println("Failed to read " + this.configFuelSlot.toString() + ". Only 'Top' and 'Bottom' are allowed as values. Using default");
                break;
        }
        return this;
    }

    @Override
    public void meetsRequirements(MovementContext context) throws MoveException {
        if (!context.isStrictMovement()) {
            return;
        }
        if (this.fuelConsumption == 0 || (this.fuelTypes.isEmpty())) {
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
            Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
            return slot.getItem().isPresent();
        }).filter(i -> {
            Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
            return slot.getItem().get().getQuantity() >= this.fuelConsumption;
        }).filter(i -> {
            Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
            return this.fuelTypes.stream().anyMatch(type -> slot.getItem().get().getType().equals(type));
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
        if (this.fuelConsumption == 0 || (this.fuelTypes.isEmpty())) {
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
            Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
            return slot.getItem().isPresent();
        }).filter(i -> {
            Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
            return slot.getItem().get().getQuantity() >= this.fuelConsumption;
        }).filter(i -> {
            Slot slot = this.takeFromTopSlot ? i.getSmeltingSlot() : i.getFuelSlot();
            return this.fuelTypes.stream().anyMatch(type -> slot.getItem().get().getType().equals(type));
        }).collect(Collectors.toList());
        if (acceptedSlots.isEmpty()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_FUEL, new RequiredFuelMovementData(this.fuelConsumption, this.fuelTypes)));
        }
        FurnaceInventory inv = acceptedSlots.get(0);
        Slot slot = this.takeFromTopSlot ? inv.getSmeltingSlot() : inv.getFuelSlot();
        ItemStack item = slot.getItem().get();
        item = item.copyWithQuantity(item.getQuantity() - this.fuelConsumption);
        if (item.getQuantity() == 0) {
            item = null;
        }
        slot.setItem(item);
    }

    @Override
    public boolean shouldFall() {
        if(this.fuelConsumption == 0 || this.fuelTypes.isEmpty()){
            return false;
        }
        Set<FurnaceInventory> furnaceInventories = new HashSet<>();
        for (BlockPosition loc : this.positionableShipsStructure.getPositions()) {
            BlockSnapshot snapshot = loc.getBlockDetails();
            Optional<TileEntitySnapshot<? extends TileEntity>> opTiled = snapshot.get(KeyedData.TILED_ENTITY);
            if (opTiled.isPresent()) {
                if (opTiled.get() instanceof FurnaceTileEntitySnapshot) {
                    furnaceInventories.add(((FurnaceTileEntitySnapshot) opTiled.get()).getInventory());
                }
            }
        }
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
        if (acceptedSlots.isEmpty()) {
            return true;
        }
        return false;
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
