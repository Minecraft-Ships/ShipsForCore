package org.ships.vessel.common.assits;

import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.SetMovingBlock;
import org.ships.movement.instruction.MovementInstructionBuilder;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.movement.instruction.details.SimpleMovementException;

public interface FallableRequirementVessel extends Fallable, VesselRequirement {

    @Override
    default boolean shouldFall() {
        MovementContext movementContext = new MovementContext(
                new MovementDetailsBuilder().setException(new SimpleMovementException()).build(),
                new MovementInstructionBuilder()
                        .setMovementBlocks(this.getStructure(), b -> new SetMovingBlock(b, b))
                        .build());
        return this.getRequirements().stream().noneMatch(requirement -> {
            try {
                requirement.onCheckRequirement(movementContext, this);
                return true;
            } catch (MoveException e) {
                return false;
            }
        });


    }
}
