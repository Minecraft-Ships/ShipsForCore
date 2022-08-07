package org.ships.commands.argument.ship.data.speed.max;

import org.core.adventureText.AText;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.ships.commands.argument.ship.data.AbstractShipsDataViewCommand;
import org.ships.vessel.common.types.Vessel;

import java.util.List;

public class ShipsDataViewMaxSpeedCommand extends AbstractShipsDataViewCommand {


    private final ExactArgument SPEED_ARGUMENT = new ExactArgument("speed");

    @Override
    protected List<CommandArgument<?>> getExtraArguments() {
        return List.of(SPEED_ARGUMENT);
    }

    @Override
    protected AText getValue(CommandContext commandContext, Vessel vessel, String[] arguments) {
        int speed = vessel.getMaxSpeed();
        return AText.ofPlain("Max Speed: " + speed);
    }
}