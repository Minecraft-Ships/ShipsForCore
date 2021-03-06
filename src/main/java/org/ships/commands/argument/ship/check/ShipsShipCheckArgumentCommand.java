package org.ships.commands.argument.ship.check;

import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.SetMovingBlock;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.assits.Fallable;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.types.Vessel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShipsShipCheckArgumentCommand implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_CHECK_ARGUMENT = "check";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_ARGUMENT), new ShipIdArgument<>(SHIP_ID_ARGUMENT, (source, vessel) -> {
            if (vessel instanceof Fallable) {
                return true;
            }
            if (source instanceof LivePlayer && vessel instanceof CrewStoredVessel) {
                CrewStoredVessel crewVessel = (CrewStoredVessel) vessel;
                LivePlayer player = (LivePlayer) source;
                return crewVessel.getPermission(player.getUniqueId()).canCommand();
            }
            return vessel instanceof VesselRequirement;
        }, vessel -> "Does not have any requirements"), new ExactArgument(SHIP_CHECK_ARGUMENT));
    }

    @Override
    public String getDescription() {
        return "Checks the requirement of a ship";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource source = commandContext.getSource();
        if (!(source instanceof CommandViewer)) {
            return false;
        }
        CommandViewer viewer = (CommandViewer) source;
        Vessel vessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);
        if (vessel instanceof Fallable) {
            Fallable fVessel = (Fallable) vessel;
            viewer.sendMessagePlain("Will Fall: " + fVessel.shouldFall());
        }
        if (vessel instanceof VesselRequirement) {
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
