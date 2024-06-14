package org.ships.commands.argument.ship.check;

import net.kyori.adventure.text.Component;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.User;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.SetMovingBlock;
import org.ships.movement.instruction.MovementInstructionBuilder;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.movement.instruction.details.SimpleMovementException;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.assits.Fallable;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.types.Vessel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShipsShipCheckArgumentCommand implements ArgumentCommand {

    public static final String SHIP_ARGUMENT = "ship";
    public static final String SHIP_ID_ARGUMENT = "ship_id";
    public static final String SHIP_CHECK_ARGUMENT = "check";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_ARGUMENT),
                             new ShipIdArgument<>(SHIP_ID_ARGUMENT, (source, vessel) -> {
                                 if (!(vessel instanceof Fallable)) {
                                     return false;
                                 }
                                 if (!(vessel instanceof VesselRequirement)) {
                                     return false;
                                 }
                                 if (source instanceof LivePlayer && Permissions.CMD_SHIP_TRACK_OWN.hasPermission(
                                         (LivePlayer) source)) {
                                     return true;
                                 } else if ((source instanceof User && vessel instanceof CrewStoredVessel)) {
                                     return ((CrewStoredVessel) vessel)
                                             .getPermission(((User) source).getUniqueId())
                                             .canCommand();
                                 }
                                 return false;
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
        Vessel vessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);
        if (vessel instanceof Fallable) {
            source.sendMessage(Component.text("Will Fall: " + ((Fallable) vessel).shouldFall()));
        }
        if (!(vessel instanceof VesselRequirement)) {
            return true;
        }
        VesselRequirement rVessel = (VesselRequirement) vessel;
        MovingBlockSet set = rVessel
                .getStructure()
                .getSyncedPositionsRelativeToWorld()
                .stream()
                .map(block -> new SetMovingBlock(block, block))
                .collect(Collectors.toCollection(MovingBlockSet::new));

        MovementContext context = new MovementContext(
                new MovementDetailsBuilder().setException(new SimpleMovementException()).build(),
                new MovementInstructionBuilder().setMovingBlocks(set).setStrictMovement(true).build());

        try {
            rVessel.checkRequirements(context);
            source.sendMessage(Component.text("Meets Requirements: true"));
        } catch (MoveException e) {
            source.sendMessage(Component.text("Meets Requirements: false"));
            source.sendMessage(e.getErrorMessage());
        }

        return true;
    }
}
