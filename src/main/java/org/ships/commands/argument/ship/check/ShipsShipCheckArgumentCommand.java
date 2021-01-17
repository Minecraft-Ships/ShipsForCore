package org.ships.commands.argument.ship.check;

import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.commands.argument.type.ShipIdArgument;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.SetMovingBlock;
import org.ships.vessel.common.assits.Fallable;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.types.Vessel;

import java.util.Arrays;
import java.util.List;

public class ShipsShipCheckArgumentCommand implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_CHECK_ARGUMENT = "check";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_ARGUMENT), new ShipIdArgument<>(SHIP_ID_ARGUMENT, vessel -> {
            if(vessel instanceof Fallable){
                return true;
            }
            return vessel instanceof VesselRequirement;
        }, vessel -> "Does not have any requirements"), new ExactArgument(SHIP_CHECK_ARGUMENT));
    }

    @Override
    public String getDescription() {
        return "Checks the requirement of a ship";
    }

    @Override
    public String getPermissionNode() {
        return "";
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource source = commandContext.getSource();
        if(!(source instanceof CommandViewer)){
            return false;
        }
        CommandViewer viewer = (CommandViewer)source;
        Vessel vessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);
        if(vessel instanceof Fallable){
            Fallable fVessel = (Fallable)vessel;
            viewer.sendMessagePlain("Will Fall: " + fVessel.shouldFall());
        }
        if(vessel instanceof VesselRequirement) {
            VesselRequirement rVessel = (VesselRequirement) vessel;
            MovementContext context = new MovementContext();
            MovingBlockSet set = new MovingBlockSet();
            for (SyncBlockPosition position : rVessel.getStructure().getPositions()) {
                set.add(new SetMovingBlock(position, position));
            }
            context.setMovingStructure(set);
            context.setStrictMovement(true);
            try {
                rVessel.meetsRequirements(context);
                viewer.sendMessagePlain("Meets Requirements: true");
            } catch (MoveException e) {
                viewer.sendMessagePlain("Meets Requirements: False");
                e.getMovement().sendMessage(viewer);
            }
        }
        return true;
    }
}
