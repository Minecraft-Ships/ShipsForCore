package org.ships.movement.result;

import org.core.source.viewer.CommandViewer;
import org.core.world.position.BlockPosition;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public interface MovementResult<E extends Object> {

    NoSpeedSet NO_SPEED_SET = new NoSpeedSet();
    CollideDetected COLLIDE_DETECTED = new CollideDetected();
    NoLicenceFound NO_LICENCE_FOUND = new NoLicenceFound();
    Unknown UNKNOWN = new Unknown();

    void sendMessage(Vessel vessel, CommandViewer viewer, E value);
    boolean isARequiredValue(Vessel vessel, CommandViewer viewer, E value);

    class Unknown implements MovementResult<Boolean> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer player, Boolean value) {
            player.sendMessage("A Unknown Error Occurred");

        }

        @Override
        public boolean isARequiredValue(Vessel vessel, CommandViewer player, Boolean value) {
            return false;
        }
    }

    class NoLicenceFound implements MovementResult<Boolean> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, Boolean value) {
            viewer.sendMessage("Failed to find licence sign for ship");
        }

        @Override
        public boolean isARequiredValue(Vessel vessel, CommandViewer viewer, Boolean value) {
            return false;
        }
    }

    class CollideDetected implements MovementResult<Collection<BlockPosition>> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, Collection<BlockPosition> collection) {
            Set<String> blocks = new HashSet<>();
            collection.stream().forEach(s -> {
                String value = s.getBlockType().getName();
                if(blocks.contains(value)){
                    return;
                }
                blocks.add(value);
            });
            viewer.sendMessage("Found the following blocks in the way: " + ShipsPlugin.toString(blocks, ", ", b -> b));
        }

        @Override
        public boolean isARequiredValue(Vessel vessel, CommandViewer viewer, Collection<BlockPosition> value) {
            return false;
        }
    }

    class NoSpeedSet implements MovementResult<Integer> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer player, Integer value) {
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
