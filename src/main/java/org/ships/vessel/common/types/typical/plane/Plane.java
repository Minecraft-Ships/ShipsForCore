package org.ships.vessel.common.types.typical.plane;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.config.parser.parsers.StringToEnumParser;
import org.core.inventory.inventories.general.block.FurnaceInventory;
import org.core.inventory.item.ItemType;
import org.core.inventory.item.ItemTypes;
import org.core.inventory.item.stack.ItemStack;
import org.core.inventory.parts.Slot;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.BlockSnapshot;
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
import org.ships.movement.autopilot.FlightPath;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.movement.result.data.RequiredFuelMovementData;
import org.ships.vessel.common.assits.*;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.requirement.Requirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;
import java.util.stream.Collectors;

public class Plane extends AbstractShipsVessel implements AirType, VesselRequirement, Fallable, FlightPathType {

    protected @Nullable Integer fuelConsumption;
    protected @Nullable FuelSlot fuelSlot;
    protected @NotNull Set<ItemType> fuelTypes = new HashSet<>();
    protected @Nullable FlightPath path;

    protected final ConfigurationNode.KnownParser.SingleKnown<Integer> configFuelConsumption =
            new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Block", "Fuel", "Consumption");
    protected final ConfigurationNode.KnownParser.SingleKnown<FuelSlot> configFuelSlot =
            new ConfigurationNode.KnownParser.SingleKnown<>(new StringToEnumParser<>(FuelSlot.class), "Block", "Fuel", "Slot");
    protected final ConfigurationNode.KnownParser.CollectionKnown<ItemType> configFuelTypes =
            new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_ITEM_TYPE, "Block", "Fuel", "Types");

    public Plane(LiveTileEntity licence, ShipType<? extends Plane> type) throws NoLicencePresent {
        super(licence, type);
    }

    public Plane(SignTileEntity ste, SyncBlockPosition position, ShipType<? extends Plane> type) {
        super(ste, position, type);
    }

    public Set<ItemType> getFuelTypes() {
        if (this.fuelTypes.isEmpty()) {
            return this.getType().getDefaultFuelTypes();
        }
        return this.fuelTypes;
    }

    public int getFuelConsumption() {
        if (this.fuelConsumption != null) {
            return this.fuelConsumption;
        }
        return this.getType().getDefaultFuelConsumption();
    }

    public FuelSlot getFuelSlot() {
        if (this.fuelSlot == null) {
            return this.getType().getDefaultFuelSlot();
        }
        return this.fuelSlot;
    }

    @Override
    public @NotNull PlaneType getType() {
        return (PlaneType) super.getType();
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
        return map;
    }

    @Override
    public Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(ConfigurationStream file) {
        Map<ConfigurationNode.KnownParser<?, ?>, Object> map = new HashMap<>();
        map.put(this.configFuelConsumption, this.getFuelConsumption());
        map.put(this.configFuelSlot, this.getFuelSlot());
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
    public Collection<Requirement> getRequirements() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void meetsRequirements(MovementContext context) throws MoveException {
        VesselRequirement.super.meetsRequirements(context);
        if (!context.isStrictMovement()) {
            return;
        }
        if (this.getFuelConsumption() == 0 || (this.getFuelTypes().isEmpty())) {
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
        List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
            Slot slot = this.getFuelSlot() == FuelSlot.TOP ? i.getSmeltingSlot() : i.getFuelSlot();
            return slot.getItem().isPresent();
        }).filter(i -> {
            Slot slot = this.getFuelSlot() == FuelSlot.TOP ? i.getSmeltingSlot() : i.getFuelSlot();
            return slot.getItem().map(ItemStack::getQuantity).orElse(0) >= this.getFuelConsumption();
        }).filter(i -> {
            Slot slot = this.getFuelSlot() == FuelSlot.TOP ? i.getSmeltingSlot() : i.getFuelSlot();
            return this.getFuelTypes().stream().anyMatch(type -> slot.getItem().map(t -> type.equals(t.getType())).orElse(false));
        }).collect(Collectors.toList());
        if (acceptedSlots.isEmpty()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_FUEL, new RequiredFuelMovementData(this.getFuelConsumption(), this.getFuelTypes())));
        }
    }

    @Override
    public void processRequirements(MovementContext context) throws MoveException {
        VesselRequirement.super.processRequirements(context);
        if (!context.isStrictMovement()) {
            return;
        }
        if (this.getFuelConsumption() == 0 || (this.getFuelTypes().isEmpty())) {
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
        List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
            Slot slot = this.getFuelSlot() == FuelSlot.TOP ? i.getSmeltingSlot() : i.getFuelSlot();
            return slot.getItem().isPresent();
        }).filter(i -> {
            Slot slot = this.getFuelSlot() == FuelSlot.TOP ? i.getSmeltingSlot() : i.getFuelSlot();
            return slot.getItem().map(ItemStack::getQuantity).orElse(0) >= (this.fuelConsumption == null ? 0 : this.fuelConsumption);
        }).filter(i -> {
            Slot slot = this.getFuelSlot() == FuelSlot.TOP ? i.getSmeltingSlot() : i.getFuelSlot();
            return this.getFuelTypes().stream().anyMatch(type -> slot.getItem().map(t -> t.getType().equals(type)).orElse(false));
        }).collect(Collectors.toList());
        if (acceptedSlots.isEmpty()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_FUEL, new RequiredFuelMovementData(this.getFuelConsumption(), this.getFuelTypes())));
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

    @Override
    public void setRequirement(Requirement updated) {
        throw new RuntimeException("Not implemented yet");

    }

    @Override
    public boolean shouldFall() {
        if (this.getFuelConsumption() == 0 || this.getFuelTypes().isEmpty()) {
            return false;
        }
        Collection<FurnaceInventory> furnaceInventories = new HashSet<>();
        for (SyncBlockPosition loc : this.getStructure().getPositions()) {
            BlockSnapshot<SyncBlockPosition> snapshot = loc.getBlockDetails();
            Optional<TileEntitySnapshot<? extends TileEntity>> opTiled = snapshot.get(KeyedData.TILED_ENTITY);
            if (opTiled.isPresent()) {
                if (opTiled.get() instanceof FurnaceTileEntitySnapshot) {
                    furnaceInventories.add(((FurnaceTileEntity) opTiled.get()).getInventory());
                }
            }
        }
        List<FurnaceInventory> acceptedSlots = furnaceInventories.stream().filter(i -> {
            Slot slot = this.getFuelSlot() == FuelSlot.TOP ? i.getSmeltingSlot() : i.getFuelSlot();
            return slot.getItem().isPresent();
        }).filter(i -> {
            Slot slot = this.getFuelSlot() == FuelSlot.TOP ? i.getSmeltingSlot() : i.getFuelSlot();
            return slot.getItem().map(ItemStack::getQuantity).orElse(0) >= this.getFuelConsumption();
        }).filter(i -> {
            Slot slot = this.getFuelSlot() == FuelSlot.TOP ? i.getSmeltingSlot() : i.getFuelSlot();
            return this.getFuelTypes().stream().anyMatch(type -> slot.getItem().map(item -> item.getType().equals(type)).orElse(false));
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
