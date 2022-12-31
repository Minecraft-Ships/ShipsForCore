package org.ships.movement.autopilot.scheduler;

import org.core.schedule.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.ships.movement.autopilot.path.FlightCheckpoint;
import org.ships.movement.autopilot.path.FlightPath;
import org.ships.vessel.common.flag.FlightPathFlag;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;
import java.util.function.Consumer;

public class AutopilotExecutor implements Consumer<Scheduler> {

    private final @NotNull Vessel vessel;

    public AutopilotExecutor(@NotNull Vessel vessel) {
        this.vessel = vessel;
    }

    @Override
    public void accept(Scheduler t) {
        Optional<FlightPathFlag> opFlightPathFlag = this.vessel.get(FlightPathFlag.class);
        if (opFlightPathFlag.isEmpty()) {
            return;
        }
        Optional<FlightPath> opFlightPath = opFlightPathFlag.get().getValue();
        if (opFlightPath.isEmpty()) {
            return;
        }

        Optional<FlightCheckpoint> opCheckpoint = opFlightPathFlag.get().getCurrentCheckpoint();
        FlightCheckpoint nextCheckpoint = opCheckpoint
                .flatMap(c -> opFlightPath.get().getNext(c))
                .orElseGet(() -> opFlightPath.get().getNearest(this.vessel.getPosition().getPosition()));
        if (nextCheckpoint.hasReached(this.vessel)) {
            Optional<FlightCheckpoint> opNewCheckout = opFlightPath.get().getNext(nextCheckpoint);
            if (opNewCheckout.isEmpty()) {
                this.vessel.set(new FlightPathFlag());
                if(t instanceof Scheduler.Native nativeSch){
                    nativeSch.cancel();
                }
                return;
            }
            nextCheckpoint = opNewCheckout.get();
        }
        opFlightPathFlag.get().setCurrentCheckpoint(nextCheckpoint);
        this.vessel.moveTowards(nextCheckpoint.getPosition().getPosition(),
                                opFlightPathFlag.get().getMovementDetail().build());

    }
}
