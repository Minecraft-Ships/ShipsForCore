package org.ships.commands.argument.ship.data.speed.max;

import org.core.adventureText.AText;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.arguments.simple.number.IntegerArgument;
import org.core.command.argument.context.CommandContext;
import org.core.source.viewer.CommandViewer;
import org.core.utils.Else;
import org.ships.commands.argument.ship.data.AbstractShipsDataSetCommand;
import org.ships.exceptions.NoLicencePresent;
import org.ships.vessel.common.types.Vessel;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ShipsDataSetMaxSpeedCommand extends AbstractShipsDataSetCommand {

    private final OptionalArgument<Integer> SPEED_VALUE_ARGUMENT = new OptionalArgument<>(
            new IntegerArgument("speed_value"), (Integer) null);
    private final ExactArgument SPEED_ARGUMENT = new ExactArgument("speed");


    @Override
    protected List<CommandArgument<?>> getExtraArguments() {
        return Arrays.asList(
                this.SPEED_ARGUMENT,
                this.SPEED_VALUE_ARGUMENT
        );
    }

    @Override
    protected boolean apply(CommandContext commandContext, Vessel vessel, String[] arguments) {
        Integer value = commandContext.getArgument(this, this.SPEED_VALUE_ARGUMENT);
        vessel.setMaxSpeed(value);
        if (commandContext.getSource() instanceof CommandViewer viewer) {
            int displayValue = Objects.requireNonNullElseGet(value, () -> vessel.getType().getDefaultMaxSpeed());
            viewer.sendMessage(AText.ofPlain(
                    "Updated " + Else.throwOr(NoLicencePresent.class, vessel::getName, "Unknown") + " max speed to " +
                            displayValue));
        }
        return true;
    }
}
