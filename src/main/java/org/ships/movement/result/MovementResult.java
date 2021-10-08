package org.ships.movement.result;

import org.array.utils.ArrayUtils;
import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.config.parser.Parser;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.Scheduler;
import org.core.schedule.unit.TimeUnit;
import org.core.source.viewer.CommandViewer;
import org.core.utils.Identifiable;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.exceptions.NoLicencePresent;
import org.ships.movement.result.data.RequiredFuelMovementData;
import org.ships.movement.result.data.RequiredPercentMovementData;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.IdentifiableShip;
import org.ships.vessel.common.types.Vessel;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface MovementResult<E> {

    NoSpeedSet NO_SPEED_SET = new NoSpeedSet();
    OverSized OVER_SIZED = new OverSized();
    UnderSized UNDER_SIZED = new UnderSized();
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

    class OverSized implements MovementResult<Integer> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, Integer value) {
            AText errorMessage = AdventureMessageConfig
                    .ERROR_OVERSIZED
                    .process(
                            AdventureMessageConfig
                                    .ERROR_OVERSIZED
                                    .parse(ShipsPlugin.getPlugin().getAdventureMessageConfig()),
                            new AbstractMap.SimpleImmutableEntry<>(vessel, value));
            viewer.sendMessage(errorMessage);
        }
    }

    class UnderSized implements MovementResult<Integer> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, Integer value) {
            AText errorMessage = AdventureMessageConfig.ERROR_UNDERSIZED.process(AdventureMessageConfig.ERROR_UNDERSIZED.parse(ShipsPlugin.getPlugin().getAdventureMessageConfig()), new AbstractMap.SimpleImmutableEntry<>(vessel, value));
            viewer.sendMessage(errorMessage);
        }
    }

    class TooManyOfBlock implements MovementResult<BlockType> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, BlockType value) {
            AText errorMessage = AdventureMessageConfig.ERROR_TOO_MANY_OF_BLOCK.process(AdventureMessageConfig.ERROR_TOO_MANY_OF_BLOCK.parse(ShipsPlugin.getPlugin().getAdventureMessageConfig()), new AbstractMap.SimpleImmutableEntry<>(vessel, value));
            viewer.sendMessage(errorMessage);
        }
    }

    class VesselMovingAlready implements MovementResult<Boolean> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, Boolean value) {
            AText errorMessage = AdventureMessageConfig.ERROR_ALREADY_MOVING.process(AdventureMessageConfig.ERROR_ALREADY_MOVING.parse(ShipsPlugin.getPlugin().getAdventureMessageConfig()), vessel);
            viewer.sendMessage(errorMessage);
        }
    }

    class VesselStillLoading implements MovementResult<Boolean> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, Boolean value) {
            AText errorMessage = AdventureMessageConfig.ERROR_VESSEL_STILL_LOADING.process(AdventureMessageConfig.ERROR_VESSEL_STILL_LOADING.parse(ShipsPlugin.getPlugin().getAdventureMessageConfig()), vessel);
            viewer.sendMessage(errorMessage);
        }
    }

    class NoMovingToFound implements MovementResult<Collection<BlockType>> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, Collection<BlockType> value) {
            String message = ShipsPlugin.getPlugin().getMessageConfig().getNotInMovingIn();
            if (message.contains("%Block Names%")) {
                message = message.replaceAll("%Block Names%", ArrayUtils.toString(",", Identifiable::getName, value));
            }
            if (message.contains("%Block Ids%")) {
                message = message.replaceAll("%Block Ids%", ArrayUtils.toString(",", Identifiable::getId, value));
            }
            viewer.sendMessagePlain(formatMessage(message, vessel, viewer));
        }
    }

    class NotEnoughFuel implements MovementResult<RequiredFuelMovementData> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, RequiredFuelMovementData value) {
            viewer.sendMessagePlain("Your ship does not have " + value.getRequiredConsumption() + " fuel of " + ArrayUtils.toString(", ", t -> t, Parser.unparseList(Parser.STRING_TO_ITEM_TYPE, value.getAcceptedFuels())) + " in a single furnace");
        }
    }

    class NotEnoughPercent implements MovementResult<RequiredPercentMovementData> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, RequiredPercentMovementData value) {
            AText text = AText.ofPlain("Your ship has " + value.getHas() + "% of " + value.getBlockType().getName() + ". You need " + value.getRequired() + "% or more.");
            viewer.sendMessage(text);
        }
    }

    class NoSpecialBlockFound implements MovementResult<BlockType> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer viewer, BlockType value) {
            String message = ShipsPlugin.getPlugin().getMessageConfig().getFailedToFindSpecialBlock();
            message = message.replaceAll("%Block Name%", value.getName());
            message = message.replaceAll("%Block Id%", value.getId());
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
            /*AText text = AdventureMessageConfig.ERROR_BLOCK_IN_WAY.process(new AbstractMap.SimpleImmutableEntry<>(vessel, collection));
            viewer.sendMessage(text);*/

            AText text;
            if (collection.size() == 1) {
                text = AText.ofPlain("Found a single block in the way of " + collection.iterator().next().getBlockType().getName());
            } else {
                text = AText.ofPlain("Found " + collection.size() + " blocks in the way including " + collection.iterator().next().getBlockType().getName());
            }
            viewer.sendMessage(text);

            if (!(viewer instanceof LivePlayer)) {
                return;
            }

            LivePlayer player = (LivePlayer) viewer;

            Scheduler scheduler = TranslateCore
                    .createSchedulerBuilder()
                    .setDisplayName("init display collide")
                    .setExecutor(() -> {
                    })
                    .setDelay(0)
                    .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                    .build(ShipsPlugin.getPlugin());

            List<SyncBlockPosition> list = collection.stream().map(Position::toSync).collect(Collectors.toList());

            boolean toBedrock = false;
            for (int A = 0; A < 5; A++) {
                final boolean finalToBedrock = toBedrock;
                scheduler = TranslateCore.createSchedulerBuilder()
                        .setToRunAfter(scheduler)
                        .setDelay(6)
                        .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                        .setDisplayName("Display Block: " + A)
                        .setExecutor(() -> list.forEach(bp -> {
                            if (finalToBedrock) {
                                bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player);
                            } else {
                                bp.resetBlock(player);
                            }
                        }))
                        .build(ShipsPlugin.getPlugin());
                toBedrock = !toBedrock;
            }
            scheduler.run();
        }
    }

    class NoSpeedSet implements MovementResult<Integer> {

        @Override
        public void sendMessage(Vessel vessel, CommandViewer player, Integer value) {
            String message = ShipsPlugin.getPlugin().getMessageConfig().getNoSpeedSet();
            player.sendMessagePlain(formatMessage(message, vessel, player));
        }
    }

    static String formatMessage(String message, Vessel vessel, CommandViewer viewer) {
        try {
            message = message.replaceAll("%Vessel Name%", vessel.getName());
        } catch (NoLicencePresent e) {
            message = message.replaceAll("%Vessel Name%", "Unknown");
        }
        try {
            if (vessel instanceof IdentifiableShip) {
                message = message.replaceAll("%Vessel Id%", ((IdentifiableShip) vessel).getId());
            } else {
                message = message.replaceAll("%Vessel Id%", vessel.getName().toLowerCase());
            }
        } catch (NoLicencePresent e) {
            message = message.replaceAll("%Vessel Id%", "Unknown");
        }
        if (viewer instanceof LivePlayer) {
            message = message.replaceAll("%Player Name%", ((LivePlayer) viewer).getName());
        } else {
            message = message.replaceAll("%Player Name%", "_");
        }
        return message;
    }


}
