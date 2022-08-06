package org.ships.commands.argument.ship.data;

import org.core.adventureText.AText;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.types.Vessel;

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

    protected abstract AText getValue(CommandContext commandContext, Vessel vessel, String[] arguments);

    @Override
    public List<CommandArgument<?>> getArguments() {
        List<CommandArgument<?>> arguments = Arrays.asList(
                SHIP_ARGUMENT,
                SHIP_ID_ARGUMENT,
                DATA_ARGUMENT,
                SPEED_ARGUMENT,
                VIEW_ARGUMENT
        );
        arguments.addAll(this.getExtraArguments());
        return arguments;
    }

    @Override
    public String getDescription() {
        return "Modifies the data of the ship";
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        if (!(source instanceof CommandViewer)) {
            return false;
        }
        return ArgumentCommand.super.hasPermission(source);
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIP_MODIFY_SPEED);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        if (!(commandContext.getSource() instanceof CommandViewer viewer)) {
            return false;
        }
        Vessel vessel = commandContext.getArgument(this, this.SHIP_ID_ARGUMENT);
        viewer.sendMessage(this.getValue(commandContext, vessel, args));
        return true;
    }
}
