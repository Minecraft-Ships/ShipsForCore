package org.ships.commands.argument.ship.check;

import org.core.adventureText.AText;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.User;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.SetMovingBlock;
import org.ships.movement.instruction.MovementInstructionBuilder;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
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
                                 if (vessel instanceof Fallable) {
                                     return true;
                                 }
                                 if (source instanceof User player && vessel instanceof CrewStoredVessel crewVessel) {
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
        if (!(source instanceof CommandViewer viewer)) {
            return false;
        }
        Vessel vessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);
        if (vessel instanceof Fallable fVessel) {
            viewer.sendMessage(AText.ofPlain("Will Fall: " + fVessel.shouldFall()));
        }
        if (!(vessel instanceof VesselRequirement rVessel)) {
            return true;
        }
        MovingBlockSet set = rVessel
                .getStructure()
                .getSyncedPositions()
                .stream()
                .map(block -> new SetMovingBlock(block, block))
                .collect(Collectors.toCollection(MovingBlockSet::new));

        MovementContext context = new MovementContext(new MovementDetailsBuilder().setException((context1, exe) -> {
        }).build(), new MovementInstructionBuilder().setMovingBlocks(set).setStrictMovement(true).build());

        try {
            rVessel.checkRequirements(context);
            viewer.sendMessage(AText.ofPlain("Meets Requirements: true"));
        } catch (MoveException e) {
            viewer.sendMessage(AText.ofPlain("Meets Requirements: false"));
            viewer.sendMessage(e.getErrorMessageText());
        }

        return true;
    }
}
