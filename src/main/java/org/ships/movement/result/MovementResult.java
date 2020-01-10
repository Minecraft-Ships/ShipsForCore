package org.ships.movement.result;

import org.core.CorePlugin;
import org.core.configuration.parser.Parser;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.viewer.CommandViewer;
import org.core.utils.Identifable;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.ships.movement.result.data.RequiredFuelMovementData;
import org.ships.movement.result.data.RequiredPercentMovementData;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public interface MovementResult<E> {

    NoSpeedSet NO_SPEED_SET = new NoSpeedSet();
    VesselMovingAlready VESSEL_MOVING_ALREADY = new VesselMovingAlready();
    VesselStillLoading VESSEL_STILL_LOADING = new VesselStillLoading();
    NoMovingToFound NO_MOVING_TO_FOUND = new NoMovingToFound();
    @Deprecated
    NoBurnerFound NO_BURNER_FOUND = new NoBurnerFound();
    NoSpecialBlockFound NO_SPECIAL_BLOCK_FOUND = new NoSpecialBlockFound();
    NoSpecialNamedBlockFound NO_SPECIAL_NAMED_BLOCK_FOUND = new NoSpecialNamedBlockFound();
    CollideDetected COLLIDE_DETECTED = new CollideDetected();
    NoLicenceFound NO_LICENCE_FOUND = new NoLicenceFound();
    NotEnoughPercent NOT_ENOUGH_PERCENT = new NotEnoughPercent();
    NotEnoughFuel NOT_ENOUGH_FUEL = new NotEnoughFuel();
    TooManyOfBlock TOO_MANY_OF_BLOCK = new TooManyOfBlock();
    Unknown UNKNOWN = new Unknown();

    void sendMessage(Vessel vessel, CommandViewer viewer, E value);

    class TooManyOfBlock implements MovementResult<BlockType> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, BlockType value) {
            String message = ShipsPlugin.getPlugin().getMessageConfig().getTooManyBlocks();
            message = message.replaceAll("%Block Name%", value.getName());
            message = message.replaceAll("%Block ID%", value.getId());
            viewer.sendMessagePlain(formatMessage(message, vessel, viewer));
        }
    }

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

    class NoSpecialBlockFound implements MovementResult<BlockType> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, BlockType value) {
            String message = ShipsPlugin.getPlugin().getMessageConfig().getFailedToFindSpecialBlock();
            message = message.replaceAll("%Block Name%", value.getName());
            message = message.replaceAll("%Block ID%", value.getId());
            viewer.sendMessagePlain(formatMessage(message, vessel, viewer));
        }
    }

    class NoSpecialNamedBlockFound implements MovementResult<String> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, String value) {
            String message = ShipsPlugin.getPlugin().getMessageConfig().getFailedToFindSpecialBlock();
            message = message.replaceAll("%Block Name%", value);
            viewer.sendMessagePlain(formatMessage(message, vessel, viewer));
        }
    }

    @Deprecated
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
            String message = ShipsPlugin.getPlugin().getMessageConfig().getFailedToFindLicenceSign();
            viewer.sendMessagePlain(formatMessage(message, vessel, viewer));
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
            String message = ShipsPlugin.getPlugin().getMessageConfig().getNoSpeedSet();
            player.sendMessagePlain(formatMessage(message, vessel, player));
        }
    }

    static String formatMessage(String message, Vessel vessel, CommandViewer viewer){
        message = message.replaceAll("%Vessel Name%", vessel.getName());
        if (vessel instanceof Identifable){
            message = message.replaceAll("%Vessel ID%", ((Identifable)vessel).getId());
        }else{
            message = message.replaceAll("%Vessel Name%", vessel.getName().toLowerCase());
        }
        if (viewer instanceof LivePlayer){
            message.replaceAll("%Player Name%", ((LivePlayer)viewer).getName());
        }else{
            message.replaceAll("%Player Name%", "_");
        }
        return message;
    }


}
