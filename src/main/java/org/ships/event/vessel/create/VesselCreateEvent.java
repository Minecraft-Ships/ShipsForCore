package org.ships.event.vessel.create;

import org.core.entity.living.human.player.LivePlayer;
import org.core.event.events.Cancellable;
import org.core.event.events.entity.EntityEvent;
import org.ships.event.vessel.VesselEvent;
import org.ships.vessel.common.types.Vessel;

public class VesselCreateEvent implements VesselEvent {

    private final Vessel vessel;

    public abstract static class Pre extends VesselCreateEvent implements Cancellable {

        public static class BySign extends Pre implements EntityEvent<LivePlayer> {

            private final LivePlayer player;

            public BySign(Vessel vessel, LivePlayer player) {
                super(vessel);
                this.player = player;
            }

            @Override
            public LivePlayer getEntity() {
                return this.player;
            }
        }

        private boolean cancelled;

        public Pre(Vessel vessel) {
            super(vessel);
        }


        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(boolean value) {
            this.cancelled = value;
        }
    }

    public abstract static class Post extends VesselCreateEvent {

        public static class BySign extends Post implements EntityEvent<LivePlayer> {

            private final LivePlayer player;

            public BySign(Vessel vessel, LivePlayer player) {
                super(vessel);
                this.player = player;
            }

            @Override
            public LivePlayer getEntity() {
                return this.player;
            }
        }

        public Post(Vessel vessel) {
            super(vessel);
        }
    }

    protected VesselCreateEvent(Vessel vessel) {
        this.vessel = vessel;
    }

    @Override
    public Vessel getVessel() {
        return this.vessel;
    }
}
