package org.ships.commands.argument.ship.data.block.special.percent;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.arguments.simple.number.DoubleArgument;
import org.core.command.argument.arguments.simple.number.FloatArgument;
import org.core.command.argument.context.CommandContext;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.commands.argument.ship.data.AbstractShipsDataSetCommand;
import org.ships.vessel.common.assits.requirement.SpecialBlockRequirement;
import org.ships.vessel.common.assits.shiptype.SpecialBlockShipType;
import org.ships.vessel.common.types.Vessel;

import java.util.Arrays;
import java.util.List;

public class ShipsDataSetSpecialBlockPercentCommand extends AbstractShipsDataSetCommand {

    private static final ExactArgument BLOCK_ARGUMENT = new ExactArgument("block");
    private static final ExactArgument SPECIAL_ARGUMENT = new ExactArgument("special");
    private static final ExactArgument PERCENT_ARGUMENT = new ExactArgument("percent");
    private static final OptionalArgument<Float> PERCENT_VALUE_ARGUMENT = new OptionalArgument<>(new FloatArgument("value"), (Float) null);

    public ShipsDataSetSpecialBlockPercentCommand() {
        super(new ShipIdArgument<>("ship_id", (source, vessel) -> vessel instanceof SpecialBlockRequirement, vessel -> "No special blocks apply to this ship"));
    }

    @Override
    protected List<CommandArgument<?>> getExtraArguments() {
        return Arrays.asList(BLOCK_ARGUMENT, SPECIAL_ARGUMENT, PERCENT_ARGUMENT, PERCENT_VALUE_ARGUMENT);
    }

    @Override
    protected boolean apply(CommandContext context, Vessel vessel, String[] arguments) {
        if (!(vessel instanceof SpecialBlockRequirement sbrVessel)) {
            return false;
        }
        Float value = context.getArgument(this, PERCENT_VALUE_ARGUMENT);
        if (value != null) {
            if (value > 100) {
                if (context.getSource() instanceof CommandViewer viewer) {
                    viewer.sendMessage(AText.ofPlain("Percent cannot be above 100%").withColour(NamedTextColours.RED));
                }
                return false;
            }
            if (value < 0) {
                if (context.getSource() instanceof CommandViewer viewer) {
                    viewer.sendMessage(AText.ofPlain("Percent cannot be below 0%").withColour(NamedTextColours.RED));
                }
                return false;
            }
        }
        sbrVessel.setSpecialBlocksPercent(value);
        return true;
    }
}
