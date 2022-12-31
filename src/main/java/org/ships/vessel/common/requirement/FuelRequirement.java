package org.ships.vessel.common.requirement;

import org.core.inventory.inventories.general.block.FurnaceInventory;
import org.core.inventory.item.ItemType;
import org.core.inventory.item.stack.ItemStack;
import org.core.inventory.parts.Slot;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.block.entity.container.furnace.FurnaceTileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.config.messages.messages.error.data.FuelRequirementMessageData;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.types.Vessel;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FuelRequirement implements Requirement<FuelRequirement> {

    private final @Nullable Collection<ItemType> fuelTypes;
    private final @Nullable Integer takeAmount;
    private final @Nullable FuelRequirement parent;
    private final @Nullable FuelSlot slot;

    public FuelRequirement(@NotNull FuelRequirement parent) {
        this(parent, null, null, null);
    }

    public FuelRequirement(@Nullable FuelRequirement parent,
                           @Nullable FuelSlot slot,
                           @Nullable Integer takeAmount,
                           @Nullable Collection<ItemType> fuelTypes) {
        if (parent == null && (takeAmount == null || slot == null || fuelTypes == null)) {
            throw new IllegalArgumentException("Parent must not be null if any values are null");
        }

        this.fuelTypes = fuelTypes;
        this.parent = parent;
        this.takeAmount = takeAmount;
        this.slot = slot;
    }

    public OptionalInt getSpecifiedConsumption() {
        if (this.takeAmount == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(this.takeAmount);
    }

    public int getConsumption() {
        if (this.takeAmount != null) {
            return this.takeAmount;
        }
        if (this.parent == null) {
            throw new RuntimeException("You skipped the constructor checks");

        }
        return this.parent.getConsumption();
    }

    public Optional<FuelSlot> getSpecifiedFuelSlot() {
        return Optional.ofNullable(this.slot);
    }

    public @NotNull FuelSlot getFuelSlot() {
        if (this.slot != null) {
            return this.slot;
        }
        if (this.parent == null) {
            throw new RuntimeException("You skipped the constructor checks");
        }
        return this.parent.getFuelSlot();
    }

    public @NotNull Collection<ItemType> getSpecifiedFuelTypes() {
        return this.fuelTypes == null ? Collections.emptyList() : this.fuelTypes;
    }

    public @NotNull Collection<ItemType> getFuelTypes() {
        if (this.parent == null) {
            if (this.fuelTypes == null) {
                throw new RuntimeException("You skipped the constructor checks");
            }
            return this.fuelTypes;
        }
        return this.parent.getFuelTypes();

    }


    @Override
    public boolean useOnStrict() {
        return false;
    }

    private Stream<FurnaceInventory> getInventories(MovementContext context) {
        return context
                .getMovingStructure()
                .stream()
                .map(movingBlock -> (movingBlock.getStoredBlockData()).get(KeyedData.TILED_ENTITY))
                .filter(Optional::isPresent)
                .filter(opTileEntity -> opTileEntity.get() instanceof FurnaceTileEntity)
                .map(opTileEntity -> ((FurnaceTileEntity) opTileEntity.get()).getInventory());
    }

    @Override
    public void onCheckRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {
        Collection<ItemType> fuelTypes = this.getFuelTypes();
        int toTakeAmount = this.getConsumption();
        if (fuelTypes.isEmpty() || toTakeAmount == 0) {
            return;
        }
        Collection<FurnaceInventory> furnaceInventories = this.getInventories(context).collect(Collectors.toSet());
        TreeSet<Integer> fuelSlots = furnaceInventories
                .parallelStream()
                .map(inventory -> (
                        this.slot == FuelSlot.TOP ? inventory.getSmeltingSlot() : inventory.getFuelSlot()).getItem())
                .filter(Optional::isPresent)
                .filter(opItem -> fuelTypes.contains(opItem.get().getType()))
                .map(opItem -> opItem.get().getQuantity())
                .collect(Collectors.toCollection(TreeSet::new));
        if (fuelSlots.isEmpty()) {
            throw new MoveException(context, AdventureMessageConfig.ERROR_NOT_ENOUGH_FUEL,
                                    new FuelRequirementMessageData(vessel, fuelTypes, 0));
        }
        int slot = fuelSlots.last();
        if (slot < toTakeAmount) {
            throw new MoveException(context, AdventureMessageConfig.ERROR_NOT_ENOUGH_FUEL,
                                    new FuelRequirementMessageData(vessel, fuelTypes, fuelSlots.last()));
        }
    }

    @Override
    public void onProcessRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) {
        Collection<ItemType> fuelTypes = this.getFuelTypes();
        int toTakeAmount = this.getConsumption();
        if (fuelTypes.isEmpty() || toTakeAmount == 0) {
            return;
        }
        Stream<FurnaceInventory> furnaceInventories = this.getInventories(context);

        Optional<Slot> opSlot = furnaceInventories
                .map(inventory -> (this.slot == FuelSlot.TOP ? inventory.getSmeltingSlot() : inventory.getFuelSlot()))
                .filter(slot -> slot.getItem().isPresent())
                .filter(slot -> fuelTypes.contains(slot.getItem().get().getType()))
                .filter(slot -> slot.getItem().get().getQuantity() >= toTakeAmount)
                .findAny();
        if (opSlot.isEmpty()) {
            throw new RuntimeException("onCheck was not called first or was in a different schedule");
        }
        Slot slot = opSlot.get();
        ItemStack stack = slot
                .getItem()
                .orElseThrow(() -> new RuntimeException("onCheck was not called first or was in a different schedule"));
        if ((stack.getQuantity() - toTakeAmount) > 0) {
            slot.setItem(stack.copyWithQuantity(stack.getQuantity() - toTakeAmount));
        } else {
            slot.setItem(null);
        }
    }

    @Override
    public @NotNull FuelRequirement getRequirementsBetween(@NotNull FuelRequirement requirement) {
        FuelRequirement fuel = this;
        Collection<ItemType> fuelTypes = null;
        Integer takeAmount = null;
        FuelSlot slot = null;
        while (fuel != null && fuel != requirement) {
            if (fuel.fuelTypes != null) {
                fuelTypes = (fuel.fuelTypes);
            }
            if (fuel.takeAmount != null) {
                takeAmount = fuel.takeAmount;
            }
            if (fuel.slot != null) {
                slot = fuel.slot;
            }
            fuel = fuel.getParent().orElse(null);
        }
        return new FuelRequirement(requirement, slot, takeAmount, fuelTypes);
    }

    @Override
    public @NotNull FuelRequirement createChild() {
        return new FuelRequirement(this);
    }

    @Override
    public @NotNull FuelRequirement createCopy() {
        return new FuelRequirement(this.parent, this.slot, this.takeAmount, this.fuelTypes);
    }

    public @NotNull FuelRequirement createCopyWithSlot(@Nullable FuelSlot slot) {
        return new FuelRequirement(this.parent, slot, this.takeAmount, this.fuelTypes);
    }

    public @NotNull FuelRequirement createCopyWithConsumption(@Nullable Integer amount) {
        return new FuelRequirement(this.parent, this.slot, amount, this.fuelTypes);
    }

    public @NotNull FuelRequirement createCopyWithFuel(Collection<ItemType> items) {
        return new FuelRequirement(this.parent, this.slot, this.takeAmount, items);
    }

    @Override
    public Optional<FuelRequirement> getParent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public boolean isEnabled() {
        if (this.getFuelTypes().isEmpty()) {
            return false;
        }
        return this.getConsumption() != 0;
    }
}
