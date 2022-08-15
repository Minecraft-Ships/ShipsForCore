package org.ships.vessel.common.requirement;

import org.core.inventory.inventories.live.block.LiveFurnaceInventory;
import org.core.inventory.item.ItemType;
import org.core.inventory.item.stack.ItemStack;
import org.core.inventory.parts.Slot;
import org.core.world.position.block.entity.container.furnace.LiveFurnaceTileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.movement.result.data.RequiredFuelMovementData;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class FuelRequirement implements Requirement {

    private final @Nullable Collection<ItemType> fuelTypes;
    private final @Nullable Integer takeAmount;
    private final @Nullable FuelRequirement parent;
    private final @Nullable FuelSlot slot;

    public FuelRequirement(@NotNull FuelRequirement parent) {
        this(parent, null, null, null);
    }

    public FuelRequirement(@Nullable FuelRequirement parent, @Nullable FuelSlot slot, @Nullable Integer takeAmount,
            @Nullable Collection<ItemType> fuelTypes) {
        if (parent == null && (takeAmount == null || slot == null || fuelTypes == null)) {
            throw new IllegalArgumentException("Parent must not be null if any values are null");
        }

        this.fuelTypes = fuelTypes;
        this.parent = parent;
        this.takeAmount = takeAmount;
        this.slot = slot;
    }

    public int getConsumption() {
        if (this.parent == null) {
            if (this.takeAmount == null) {
                throw new RuntimeException("You skipped the constructor checks");
            }
            return this.takeAmount;
        }
        return this.parent.getConsumption();
    }

    public @NotNull FuelSlot getFuelSlot() {
        if (this.parent == null) {
            if (this.slot == null) {
                throw new RuntimeException("You skipped the constructor checks");
            }
            return this.slot;
        }
        return this.parent.getFuelSlot();
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

    private Collection<LiveFurnaceInventory> getInventories(MovementContext context) {
        return context
                .getMovingStructure()
                .parallelStream()
                .map(movingBlock -> movingBlock.getBeforePosition().getTileEntity())
                .filter(Optional::isPresent)
                .filter(opTileEntity -> opTileEntity.get() instanceof LiveFurnaceTileEntity)
                .map(opTileEntity -> ((LiveFurnaceTileEntity) opTileEntity.get()).getInventory())
                .collect(Collectors.toSet());
    }

    @Override
    public void onCheckRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {
        Collection<ItemType> fuelTypes = this.getFuelTypes();
        int toTakeAmount = this.getConsumption();
        if (fuelTypes.isEmpty() || toTakeAmount == 0) {
            return;
        }
        Collection<LiveFurnaceInventory> furnaceInventories = this.getInventories(context);
        boolean check = furnaceInventories
                .parallelStream()
                .map(inventory -> (
                        this.slot == FuelSlot.TOP ? inventory.getSmeltingSlot() : inventory.getFuelSlot()).getItem())
                .filter(Optional::isPresent)
                .filter(opItem -> fuelTypes.contains(opItem.get().getType()))
                .map(opItem -> opItem.get().getQuantity())
                .anyMatch(amount -> amount >= toTakeAmount);
        if (!check) {
            throw new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.NOT_ENOUGH_FUEL,
                    new RequiredFuelMovementData(toTakeAmount, fuelTypes)));
        }
    }

    @Override
    public void onProcessRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {
        Collection<ItemType> fuelTypes = this.getFuelTypes();
        int toTakeAmount = this.getConsumption();
        if (fuelTypes.isEmpty() || toTakeAmount == 0) {
            return;
        }
        Collection<LiveFurnaceInventory> furnaceInventories = this.getInventories(context);

        Optional<Slot> opSlot = furnaceInventories
                .parallelStream()
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
            stack = stack.copyWithQuantity(stack.getQuantity() - toTakeAmount);
        } else {
            stack = null;
        }
        slot.setItem(stack);
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
    public Optional<Requirement> getParent() {
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
