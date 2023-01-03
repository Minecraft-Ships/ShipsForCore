package org.ships.vessel.common.types.typical.plane;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.config.parser.parsers.StringToEnumParser;
import org.core.inventory.inventories.general.block.FurnaceInventory;
import org.core.inventory.item.ItemType;
import org.core.inventory.item.stack.ItemStack;
import org.core.inventory.parts.Slot;
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
import org.ships.exceptions.NoLicencePresent;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.assits.Fallable;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.requirement.FuelRequirement;
import org.ships.vessel.common.requirement.MaxSizeRequirement;
import org.ships.vessel.common.requirement.MinSizeRequirement;
import org.ships.vessel.common.requirement.Requirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;
import java.util.stream.Collectors;

public class Plane extends AbstractShipsVessel implements AirType, VesselRequirement, Fallable {

    protected final ConfigurationNode.KnownParser.SingleKnown<Integer> configFuelConsumption = new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_INTEGER, "Block", "Fuel", "Consumption");
    protected final ConfigurationNode.KnownParser.SingleKnown<FuelSlot> configFuelSlot = new ConfigurationNode.KnownParser.SingleKnown<>(
            new StringToEnumParser<>(FuelSlot.class), "Block", "Fuel", "Slot");
    protected final ConfigurationNode.KnownParser.CollectionKnown<ItemType> configFuelTypes = new ConfigurationNode.KnownParser.CollectionKnown<>(
            Parser.STRING_TO_ITEM_TYPE, "Block", "Fuel", "Types");

    private final Collection<Requirement<?>> requirements = new HashSet<>();

    public Plane(LiveTileEntity licence, ShipType<? extends Plane> type) throws NoLicencePresent {
        super(licence, type);
        this.initRequirements();
    }

    public Plane(SignTileEntity ste, SyncBlockPosition position, ShipType<? extends Plane> type) {
        super(ste, position, type);
        this.initRequirements();
    }

    public MaxSizeRequirement getMaxBlocksRequirement() {
        return this
                .getRequirement(MaxSizeRequirement.class)
                .orElseThrow(() -> new RuntimeException("Submarine is missing a max blocks requirement"));
    }

    public MinSizeRequirement getMinBlocksRequirement() {
        return this
                .getRequirement(MinSizeRequirement.class)
                .orElseThrow(() -> new RuntimeException("Submarine is missing a min blocks requirement"));
    }

    public FuelRequirement getFuelRequirement() {
        return this
                .getRequirement(FuelRequirement.class)
                .orElseThrow(() -> new RuntimeException("Plane is missing fuel requirement"));
    }

    public Collection<ItemType> getFuelTypes() {
        return this.getFuelRequirement().getFuelTypes();
    }

    public int getFuelConsumption() {
        return this.getFuelRequirement().getConsumption();
    }

    public FuelSlot getFuelSlot() {
        return this.getFuelRequirement().getFuelSlot();
    }

    @Override
    public @NotNull PlaneType getType() {
        return (PlaneType) super.getType();
    }

    @Override
    public @NotNull Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Fuel", this
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
        FuelRequirement fuelRequirements = this.getFuelRequirement();
        Optional<Integer> opConsumption = file.getInteger(this.configFuelConsumption);
        if (opConsumption.isPresent()) {
            fuelRequirements = fuelRequirements.createCopyWithConsumption(opConsumption.get());
        }
        fuelRequirements.createCopyWithFuel(file.parseCollection(this.configFuelTypes, new HashSet<>(), null));
        fuelRequirements.createCopyWithSlot(file.parse(this.configFuelSlot).orElse(null));
        this.setRequirement(fuelRequirements);
        return this;
    }

    @Override
    public Collection<Requirement<?>> getRequirements() {
        return Collections.unmodifiableCollection(this.requirements);
    }

    @Override
    public void setRequirement(Requirement<?> updated) {
        this.getRequirement(updated.getClass()).ifPresent(this.requirements::remove);
        this.requirements.add(updated);
    }

    @Override
    public boolean shouldFall() {
        if (this.getFuelConsumption() == 0 || this.getFuelTypes().isEmpty()) {
            return false;
        }
        Collection<FurnaceInventory> furnaceInventories = new HashSet<>();
        for (SyncBlockPosition loc : this.getStructure().getSyncedPositionsRelativeToWorld()) {
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
            return this
                    .getFuelTypes()
                    .stream()
                    .anyMatch(type -> slot.getItem().map(item -> item.getType().equals(type)).orElse(false));
        }).toList();
        return acceptedSlots.isEmpty();
    }

    public @NotNull Vessel setMaxSize(@Nullable Integer size) {
        MaxSizeRequirement maxRequirements = this.getMaxBlocksRequirement();
        maxRequirements = maxRequirements.createCopy(size);
        this.setRequirement(maxRequirements);
        return this;
    }

    public boolean isMaxSizeSpecified() {
        return this.getMaxBlocksRequirement().isMaxSizeSpecified();
    }

    public @NotNull Vessel setMinSize(@Nullable Integer size) {
        MinSizeRequirement minRequirements = this.getMinBlocksRequirement();
        minRequirements = minRequirements.createCopy(size);
        this.setRequirement(minRequirements);
        return this;
    }

    public boolean isMinSizeSpecified() {
        return this.getMinBlocksRequirement().isMinSizeSpecified();
    }

}
