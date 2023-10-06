package org.ships.commands.argument.ship.data;

import net.kyori.adventure.text.Component;
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

public abstract class AbstractShipsDataViewCommand implements ArgumentCommand {


    private final ExactArgument SHIP_ARGUMENT = new ExactArgument("ship");
    private final ShipIdArgument<Vessel> SHIP_ID_ARGUMENT = new ShipIdArgument<>("ship_id");

    private final ExactArgument DATA_ARGUMENT = new ExactArgument("data");

    private final ExactArgument SPEED_ARGUMENT = new ExactArgument("speed");

    private final ExactArgument VIEW_ARGUMENT = new ExactArgument("view");

    protected abstract List<CommandArgument<?>> getExtraArguments();

    protected abstract Component getValue(CommandContext commandContext, Vessel vessel, String[] arguments);

    @Override
    public List<CommandArgument<?>> getArguments() {
        List<CommandArgument<?>> arguments = new ArrayList<>(
                Arrays.asList(this.SHIP_ARGUMENT, this.SHIP_ID_ARGUMENT, this.DATA_ARGUMENT, this.SPEED_ARGUMENT,
                              this.VIEW_ARGUMENT));
        arguments.addAll(this.getExtraArguments());
        return arguments;
    }

    @Override
    public String getDescription() {
        return "Modifies the data of the ship";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIP_MODIFY_SPEED);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        Vessel vessel = commandContext.getArgument(this, this.SHIP_ID_ARGUMENT);
        commandContext.getSource().sendMessage(this.getValue(commandContext, vessel, args));
        return true;
    }
}
