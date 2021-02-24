package org.ships.vessel.common.assits;

import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.FailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.vessel.common.types.Vessel;

public interface VesselRequirement extends Vessel {

    default void meetsRequirements(MovementContext context) throws MoveException{


    }

    default void processRequirements(MovementContext context) throws MoveException{
        int size = this.getStructure().getOriginalRelativePositions().size() + 1;
        if(this.getMaxSize().isPresent() && (this.getMaxSize().get() < size)){
            System.out.println("Size: " + size + " Max: " + this.getMaxSize().get());
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.OVER_SIZED, (size - this.getMaxSize().get())));
        }
        if(this.getMinSize() > size){
            System.out.println("Size: " + size + " Min: " + this.getMinSize());
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.UNDER_SIZED, (this.getMinSize() - size)));
        }
    }

}
