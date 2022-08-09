package org.ships.commands.argument.ship.data;

import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.types.Vessel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class AbstractShipsDataSetCommand implements ArgumentCommand {


    private final ExactArgument SHIP_ARGUMENT = new ExactArgument("ship");
    private final ShipIdArgument<? extends Vessel> SHIP_ID_ARGUMENT;

    private final ExactArgument DATA_ARGUMENT = new ExactArgument("data");

    private final ExactArgument SET_ARGUMENT = new ExactArgument("set");

    protected AbstractShipsDataSetCommand() {
        this(new ShipIdArgument<>("ship_id"));
    }

    protected AbstractShipsDataSetCommand(ShipIdArgument<? extends Vessel> argument) {
        this.SHIP_ID_ARGUMENT = argument;
    }

    protected abstract List<CommandArgument<?>> getExtraArguments();

    protected abstract boolean apply(CommandContext context, Vessel vessel, String[] arguments);

    @Override
    public List<CommandArgument<?>> getArguments() {
        List<CommandArgument<?>> originalCommands = new ArrayList<>(Arrays.asList(
                SHIP_ARGUMENT,
                SHIP_ID_ARGUMENT,
                DATA_ARGUMENT,
                SET_ARGUMENT
        ));
        originalCommands.addAll(this.getExtraArguments());
        return originalCommands;
    }

    @Override
    public String getDescription() {
        return "Modify the max speed of the ship";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIP_MODIFY_SPEED);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        Vessel vessel = commandContext.getArgument(this, this.SHIP_ID_ARGUMENT);
        return this.apply(commandContext, vessel, args);
    }
}
