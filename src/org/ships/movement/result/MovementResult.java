package org.ships.movement.result;

import org.core.CorePlugin;
import org.core.configuration.parser.Parser;
import org.core.source.viewer.CommandViewer;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.ships.movement.result.data.RequiredFuelMovementData;
import org.ships.movement.result.data.RequiredPercentMovementData;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public interface MovementResult<E> {

    NoSpeedSet NO_SPEED_SET = new NoSpeedSet();
    VesselMovingAlready VESSEL_MOVING_ALREADY = new VesselMovingAlready();
    VesselStillLoading VESSEL_STILL_LOADING = new VesselStillLoading();
    NoMovingToFound NO_MOVING_TO_FOUND = new NoMovingToFound();
    NoBurnerFound NO_BURNER_FOUND = new NoBurnerFound();
    CollideDetected COLLIDE_DETECTED = new CollideDetected();
    NoLicenceFound NO_LICENCE_FOUND = new NoLicenceFound();
    NotEnoughPercent NOT_ENOUGH_PERCENT = new NotEnoughPercent();
    NotEnoughFuel NOT_ENOUGH_FUEL = new NotEnoughFuel();
    Unknown UNKNOWN = new Unknown();

    void sendMessage(Vessel vessel, CommandViewer viewer, E value);

    class VesselMovingAlready implements MovementResult<Boolean> {



        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, Boolean value) {
            viewer.sendMessagePlain("Your vessel is already moving. Please wait for it to finish");
        }
    }

    class VesselStillLoading implements MovementResult<Boolean> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, Boolean value) {
            viewer.sendMessagePlain("Your vessel is loading. All movement controls are locked until it is loaded");
        }
    }

    class NoMovingToFound implements MovementResult<Collection<BlockType>>{

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, Collection<BlockType> value) {
            viewer.sendMessagePlain("You must be moving into one of the following blocks: " + CorePlugin.toString(", ", b -> b.getId(), value));
        }
    }

    class NotEnoughFuel implements MovementResult<RequiredFuelMovementData> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, RequiredFuelMovementData value) {
            viewer.sendMessagePlain("Your ship does not have " + value.getRequiredConsumption() + " fuel of " + CorePlugin.toString(", ", t -> t, Parser.unparseList(Parser.STRING_TO_ITEM_TYPE, value.getAcceptedFuels())) + " in a single furnace");
        }
    }

    class NotEnoughPercent implements MovementResult<RequiredPercentMovementData> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, RequiredPercentMovementData value) {
            viewer.sendMessagePlain("Your ship has " + value.getHas() + "% of " + value.getBlockType().getName() + ". You need " + value.getRequired() + "% or more.");
        }
    }

    class NoBurnerFound implements MovementResult<Boolean> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, Boolean value) {
            viewer.sendMessagePlain("Failed to find burner on ship");
        }
    }

    class Unknown implements MovementResult<Boolean> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer player, Boolean value) {
            player.sendMessagePlain("A Unknown Error Occurred");

        }
    }

    class NoLicenceFound implements MovementResult<Boolean> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, Boolean value) {
            viewer.sendMessagePlain("Failed to find licence sign for ship");
        }
    }

    class CollideDetected implements MovementResult<Collection<BlockPosition>> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, Collection<BlockPosition> collection) {
            Set<String> blocks = new HashSet<>();
            if(collection != null) {
                collection.forEach(s -> {
                    String value = s.getBlockType().getName();
                    if (blocks.contains(value)) {
                        return;
                    }
                    blocks.add(value);
                });
            }
            String value = CorePlugin.toString(", ", b -> b, blocks);
            if(value == null){
                value = "Unknown position";
            }
            viewer.sendMessagePlain("Found the following blocks in the way: " + value);
            if (collection != null) {
                collection.forEach(v -> viewer.sendMessagePlain(v.getX() + ", " + v.getY() + ", " + v.getZ()));
            }
        }
    }

    class NoSpeedSet implements MovementResult<Integer> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer player, Integer value) {
            player.sendMessagePlain("No speed speed");
        }
    }


}
