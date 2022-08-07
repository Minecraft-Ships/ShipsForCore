package org.ships.commands.argument.ship.check;

import org.core.adventureText.AText;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.User;
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
        return Arrays.asList(new ExactArgument(this.SHIP_ARGUMENT),
                new ShipIdArgument<>(this.SHIP_ID_ARGUMENT, (source, vessel) -> {
                    if (vessel instanceof Fallable) {
                        return true;
                    }
                    if (source instanceof LivePlayer && vessel instanceof CrewStoredVessel) {
                        CrewStoredVessel crewVessel = (CrewStoredVessel) vessel;
                        User player = (User) source;
                        return crewVessel.getPermission(player.getUniqueId()).canCommand();
                    }
                    return vessel instanceof VesselRequirement;
                }, vessel -> "Does not have any requirements"), new ExactArgument(this.SHIP_CHECK_ARGUMENT));
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
        Vessel vessel = commandContext.getArgument(this, this.SHIP_ID_ARGUMENT);
        if (vessel instanceof Fallable) {
            Fallable fVessel = (Fallable) vessel;
            viewer.sendMessage(AText.ofPlain("Will Fall: " + fVessel.shouldFall()));
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
                viewer.sendMessage(AText.ofPlain("Meets Requirements: true"));
            } catch (MoveException e) {
                viewer.sendMessage(AText.ofPlain("Meets Requirements: false"));
                e.getMovement().sendMessage(viewer);
            }
        }
        return true;
    }
}
