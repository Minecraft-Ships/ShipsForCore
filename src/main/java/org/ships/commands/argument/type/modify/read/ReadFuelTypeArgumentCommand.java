package org.ships.commands.argument.type.modify.read;

import org.core.adventureText.AText;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.inventory.item.ItemType;
import org.core.permission.Permission;
import org.core.source.viewer.CommandViewer;
import org.core.utils.Identifiable;
import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.shiptype.FuelledShipType;
import org.ships.vessel.common.requirement.FuelRequirement;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReadFuelTypeArgumentCommand implements ArgumentCommand {

    private final ExactArgument type = new ExactArgument("shiptype");
    private final ExactArgument modify = new ExactArgument("modify");
    private final ShipIdentifiableArgument<FuelledShipType<?>> shipType = new ShipIdentifiableArgument<>(
            "shiptype value", (Class<FuelledShipType<?>>) (Object) FuelledShipType.class, (c, a, t) -> true);
    private final ExactArgument get = new ExactArgument("get");
    private final ExactArgument fuel = new ExactArgument("fuel");


    @Override
    public List<CommandArgument<?>> getArguments() {
        return List.of(this.type, this.modify, this.shipType, this.get, this.fuel);
    }

    @Override
    public String getDescription() {
        return "Get the min and max size for a shiptype";
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

        if (commandContext.getSource() instanceof CommandViewer viewer) {
            viewer.sendMessage(AText.ofPlain("Consumption: " + take));
            viewer.sendMessage(AText.ofPlain("Slot: " + fuelSlot.name()));
            viewer.sendMessage(AText.ofPlain("Fuel Types: " + fuelTypes
                    .parallelStream()
                    .map(Identifiable::getName)
                    .collect(Collectors.joining(", "))));
        }
        return true;
    }
}
