package org.ships.event.vessel.move;

import org.core.event.events.Cancellable;
import org.ships.event.vessel.VesselEvent;
import org.ships.movement.MovementContext;
import org.ships.movement.Result;
import org.ships.vessel.common.types.Vessel;

public class ResultEvent implements VesselEvent {

    private final Vessel vessel;
    private final Result result;
    public ResultEvent(Vessel vessel, Result result) {
        this.vessel = vessel;
        this.result = result;
    }

    public Result getResult() {
        return this.result;
    }

    @Override
    public Vessel getVessel() {
        return this.vessel;
    }

    public static class PreRun extends ResultEvent implements Cancellable {

        private final Result.Run run;
        private final MovementContext context;
        private boolean isCancelled;

        public PreRun(Vessel vessel, Result result, Result.Run run, MovementContext context) {
            super(vessel, result);
            this.context = context;
            this.run = run;
        }

        public Result.Run getRun() {
            return this.run;
        }

        public MovementContext getContext() {
            return this.context;
        }

        @Override
        public boolean isCancelled() {
            return this.isCancelled;
        }

        @Override
        public void setCancelled(boolean value) {
            this.isCancelled = value;
        }
    }
}
