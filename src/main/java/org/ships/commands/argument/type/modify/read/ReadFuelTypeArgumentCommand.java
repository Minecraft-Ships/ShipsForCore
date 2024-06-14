package org.ships.commands.argument.type.modify.read;

import net.kyori.adventure.text.Component;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.inventory.item.ItemType;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.utils.Identifiable;
import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.shiptype.FuelledShipType;
import org.ships.vessel.common.requirement.FuelRequirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipTypes;
import org.ships.vessel.sign.ShipsSigns;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReadFuelTypeArgumentCommand implements ArgumentCommand {

    private final ExactArgument type = new ExactArgument("shiptype");
    private final ExactArgument modify = new ExactArgument("modify");
    private final ShipIdentifiableArgument<FuelledShipType<?>> shipType = new ShipIdentifiableArgument<>(
            "shiptype value", () -> ShipTypes
            .shipTypes()
            .stream()
            .filter(t -> t instanceof FuelledShipType)
            .map(t -> (FuelledShipType<?>) t), (c, a, t) -> true);
    private final ExactArgument get = new ExactArgument("get");
    private final ExactArgument fuel = new ExactArgument("fuel");


    @Override
    public List<CommandArgument<?>> getArguments() {
        return List.of(this.type, this.modify, this.shipType, this.get, this.fuel);
    }

    @Override
    public String getDescription() {
        return "Get the fuel information for a shiptype";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIPTYPE_MODIFY_READ);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        FuelledShipType<?> shipType = commandContext.getArgument(this, this.shipType);
        FuelRequirement fuelRequirement = shipType.getFuelRequirement();
        Collection<ItemType> fuelTypes = fuelRequirement.getFuelTypes();
        FuelSlot fuelSlot = fuelRequirement.getFuelSlot();
        int take = fuelRequirement.getConsumption();

        CommandSource viewer = commandContext.getSource();
        viewer.sendMessage(Component.text("Consumption: " + take));
        viewer.sendMessage(Component.text("Slot: " + fuelSlot.name()));
        viewer.sendMessage(Component.text("Fuel Types: " + fuelTypes
                .parallelStream()
                .map(Identifiable::getName)
                .collect(Collectors.joining(", "))));

        return true;
    }
}
