package org.ships.commands.argument.ship.autopilot;

import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.utils.Else;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.assits.FlightPathType;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CancelAutopilotCommand implements ArgumentCommand {

    private final ExactArgument SHIP_KEY = new ExactArgument("ship");
    private final ShipIdArgument<FlightPathType> SHIP = new ShipIdArgument<>("ship id", (s, v) -> v instanceof FlightPathType, v -> "Cannot use " + Else.throwOr(IOException.class, v::getName, "") + " with autopilot");
    private final ExactArgument AUTOPILOT = new ExactArgument("autopilot");
    private final ExactArgument CANCEL = new ExactArgument("cancel");

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(SHIP_KEY, SHIP, AUTOPILOT, CANCEL);
    }

    @Override
    public String getDescription() {
        return "Cancels the current autopilot task";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_AUTOPILOT);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        FlightPathType vessel = commandContext.getArgument(this, SHIP);
        vessel.setFlightPath(null);
        return true;
    }
}
