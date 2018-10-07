package org.ships.movement.result;

import org.core.source.viewer.CommandViewer;
import org.ships.vessel.common.types.Vessel;

public interface MovementResult<E extends Object> {

    NoSpeedSet NO_SPEED_SET = new NoSpeedSet();
    Unknown UNKNOWN = new Unknown();

    void sendMessage(Vessel vessel, CommandViewer viewer);
    boolean isARequiredValue(Vessel vessel, CommandViewer viewer, E value);

    class Unknown implements MovementResult<Boolean> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer player) {
            player.sendMessage("A Unknown Error Occurred");

        }

        @Override
        public boolean isARequiredValue(Vessel vessel, CommandViewer player, Boolean value) {
            return false;
        }
    }

    class NoSpeedSet implements MovementResult<Integer> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer player) {
            player.sendMessage("No speed speed");
        }

        @Override
        public boolean isARequiredValue(Vessel vessel, CommandViewer player, Integer value) {
            if(value != 0){
                return true;
            }
            return false;
        }
    }


}
