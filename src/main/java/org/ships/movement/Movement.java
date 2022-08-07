package org.ships.movement;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.exceptions.DirectionNotSupported;
import org.core.vector.type.Vector3;
import org.core.world.boss.ServerBossBar;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.DirectionalData;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;
import org.ships.config.configuration.ShipsConfig;
import org.ships.event.vessel.move.VesselMoveEvent;
import org.ships.exceptions.MoveException;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.sign.ShipsSign;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Movement {

    protected void move(Vessel vessel, MovementContext context, Consumer<? super Throwable> exception) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        if (vessel.isLoading()) {
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            exception.accept(
                    new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.VESSEL_STILL_LOADING, null)));
            return;
        }
        Optional<MovementContext> opMoving = vessel.getValue(MovingFlag.class);
        if (opMoving.isPresent()) {
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            exception.accept(new MoveException(
                    new AbstractFailedMovement<>(vessel, MovementResult.VESSEL_MOVING_ALREADY, true)));
            return;
        }
        vessel.set(MovingFlag.class, context);
        ShipsPlugin
                .getPlugin()
                .getDebugFile()
                .addMessage("Movement.68 > Started collecting entities",
                        "-\tEntityTrackingLimit: " + config.getEntityTrackingLimit());
        context.getBar().ifPresent(bar -> bar.setTitle(AText.ofPlain("Collecting entities: 0")));
        vessel.getEntitiesOvertime(config.getEntityTrackingLimit(), e -> true, e -> {
            ShipsPlugin.getPlugin().getDebugFile().addMessage("Movement.71 > Found entity " + e.getType().getId());
            EntitySnapshot<? extends LiveEntity> snapshot = e.createSnapshot();
            if (snapshot == null) {
                ShipsPlugin.getPlugin().getDebugFile().addMessage("\tMovement.74 > Failed to create snapshot");
                return;
            }
            Optional<SyncBlockPosition> opAttached = e.getAttachedTo();
            if (!opAttached.isPresent()) {
                ShipsPlugin.getPlugin().getDebugFile().addMessage("\tMovement.79 > Failed to find attached");
                return;
            }
            Optional<MovingBlock> mBlock = context.getMovingStructure().getBefore(opAttached.get());
            if (!mBlock.isPresent()) {
                SyncBlockPosition position = snapshot.getPosition().toBlockPosition();
                Collection<SyncBlockPosition> positions = vessel
                        .getStructure()
                        .getPositions((Function<? super SyncBlockPosition, ? extends SyncBlockPosition>) t -> t);
                Optional<SyncBlockPosition> opDown = positions
                        .stream()
                        .filter(f -> position.isInLineOfSight(f.getPosition(), FourFacingDirection.DOWN))
                        .findAny();
                if (!opDown.isPresent()) {
                    return;
                }
                mBlock = context.getMovingStructure().getBefore(opDown.get());
            }
            if (!mBlock.isPresent()) {
                ShipsPlugin.getPlugin().getDebugFile().addMessage("\tMovement.93 > Failed to find moving block");
                return;
            }
            context.getEntities().put(snapshot, mBlock.get());
            context
                    .getBar()
                    .ifPresent(
                            bar -> bar.setTitle(AText.ofPlain("Collecting entities: " + context.getEntities().size())));
        }, entities -> {
            ShipsPlugin.getPlugin().getDebugFile().addMessage("Movement.99 > Finished");
            context.getBar().ifPresent(bar -> {
                bar.setValue(100);
                bar.setTitle(AText.ofPlain("Processing: Pre"));
            });
            VesselMoveEvent.Pre preEvent = new VesselMoveEvent.Pre(vessel, context);
            if (TranslateCore.getPlatform().callEvent(preEvent).isCancelled()) {
                context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                context.getClicked().ifPresent(ShipsSign.LOCKED_SIGNS::remove);
                vessel.set(MovingFlag.class, null);
                return;
            }

            context.getBar().ifPresent(bar -> bar.setTitle(AText.ofPlain("Checking requirements:")));

            BlockList blockList = ShipsPlugin.getPlugin().getBlockList();

            if (config.isMovementRequirementsCheckMaxBlockType()) {
                context
                        .getBar()
                        .ifPresent(bar -> bar.setTitle(AText.ofPlain("Checking requirements: Block limit check")));
                Map<BlockType, Integer> map = new HashMap<>();
                for (MovingBlock block : context.getMovingStructure()) {
                    BlockType type = block.getStoredBlockData().getType();
                    if (map.containsKey(type)) {
                        map.replace(type, map.get(type) + 1);
                    } else {
                        map.put(type, 1);
                    }
                }
                for (Map.Entry<BlockType, Integer> entry : map.entrySet()) {
                    int limit = blockList.getBlockInstruction(entry.getKey()).getBlockLimit();
                    if (limit != -1 && entry.getValue() > limit) {
                        vessel.set(MovingFlag.class, null);
                        context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                        exception.accept(new MoveException(
                                new AbstractFailedMovement<>(vessel, MovementResult.TOO_MANY_OF_BLOCK,
                                        entry.getKey())));
                        return;
                    }
                }
            }

            context.getBar().ifPresent(bar -> bar.setTitle(AText.ofPlain("Checking requirements: valid licence")));
            Optional<MovingBlock> opLicence = context
                    .getMovingStructure()
                    .get(ShipsPlugin.getPlugin().get(LicenceSign.class).get());
            if (!opLicence.isPresent()) {
                vessel.set(MovingFlag.class, null);
                context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                exception.accept(
                        new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.NO_LICENCE_FOUND, null)));
                return;
            }
            context
                    .getBar()
                    .ifPresent(bar -> bar.setTitle(AText.ofPlain("Checking requirements: ShipType requirements")));
            if (vessel instanceof VesselRequirement) {
                try {
                    ((VesselRequirement) vessel).meetsRequirements(context);
                } catch (Throwable e) {
                    vessel.set(MovingFlag.class, null);
                    exception.accept(e);
                    return;
                }
            }
            context.getBar().ifPresent(bar -> bar.setTitle(AText.ofPlain("Checking requirements: Collide")));
            Set<SyncBlockPosition> collided = context
                    .getMovingStructure()
                    .stream()
                    .filter(mb -> {
                        SyncBlockPosition after = mb.getAfterPosition();
                        if (context
                                .getMovingStructure()
                                .stream()
                                .anyMatch(mb1 -> after.equals(mb1.getBeforePosition()))) {
                            return false;
                        }
                        for (BlockType type : vessel.getType().getIgnoredTypes()) {
                            if (type.equals(after.getBlockType())) {
                                return false;
                            }
                        }
                        BlockList list = ShipsPlugin.getPlugin().getBlockList();
                        BlockInstruction bi = list.getBlockInstruction(after.getBlockType());
                        return bi.getCollideType() != BlockInstruction.CollideType.IGNORE;
                    })
                    .map(MovingBlock::getAfterPosition)
                    .collect(Collectors.toSet());

            if (!collided.isEmpty()) {
                vessel.set(MovingFlag.class, null);
                context.getBar().ifPresent(ServerBossBar::deregisterPlayers);

                VesselMoveEvent.CollideDetected collideEvent = new VesselMoveEvent.CollideDetected(vessel, context,
                        collided);
                TranslateCore.getPlatform().callEvent(collideEvent);

                exception.accept(new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.COLLIDE_DETECTED,
                        new HashSet<>(collideEvent.getCollisions()))));
                return;
            }
            context.getBar().ifPresent(bar -> bar.setTitle(AText.ofPlain("Processing requirements:")));
            if (vessel instanceof VesselRequirement) {
                VesselRequirement requirement = (VesselRequirement) vessel;
                try {
                    requirement.processRequirements(context);
                } catch (MoveException e) {
                    exception.accept(e);
                    return;
                }
            }
            try {
                VesselMoveEvent.Main eventMain = new VesselMoveEvent.Main(vessel, context);
                if (TranslateCore.getPlatform().callEvent(eventMain).isCancelled()) {
                    return;
                }
                context.getEntities().keySet().forEach(e -> e.getCreatedFrom().get().setGravity(false));
                context.getBar().ifPresent(b -> b.setTitle(AText.ofPlain("Processing: Moving")));
                context.getMovingStructure().applyMovingBlocks();
                Result result = context.getMovement().move(vessel, context);
                context.getBar().ifPresent(b -> b.setTitle(AText.ofPlain("Processing: Post Moving")));

                result.run(vessel, context);
            } catch (Throwable e) {
                context.getEntities().keySet().forEach(entity -> entity.getCreatedFrom().get().setGravity(true));
                context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                vessel.set(MovingFlag.class, null);
                exception.accept(e);
            }
        });
    }

    public interface MidMovement {

        RotateLeftAroundPosition ROTATE_LEFT_AROUND_POSITION = new RotateLeftAroundPosition();
        RotateRightAroundPosition ROTATE_RIGHT_AROUND_POSITION = new RotateRightAroundPosition();
        AddToPosition ADD_TO_POSITION = new AddToPosition();
        TeleportToPosition TELEPORT_TO_POSITION = new TeleportToPosition();

        void move(MovingBlock moving);

    }

    public interface PostMovement {

        void postMove(Vessel vessel);

    }

    public static class RotateLeftAroundPosition extends Movement {

        private RotateLeftAroundPosition() {

        }

        public void move(Vessel vessel, SyncBlockPosition rotateAround, MovementContext context,
                Consumer<? super Throwable> exception) {
            MovingBlockSet set = new MovingBlockSet();
            vessel.getStructure().getPositions().forEach(s -> {
                MovingBlock block = new SetMovingBlock(s, s).rotateLeft(rotateAround);
                set.add(block);
            });
            context.setMovingStructure(set);
            context.setStrictMovement(true);
            context.setMidMovementProcess(mb -> {
                BlockDetails blockDetails = mb.getStoredBlockData();
                Optional<DirectionalData> opDirectional = blockDetails.getDirectionalData();
                if (opDirectional.isEmpty()) {
                    Collection<Direction> opData =
                            blockDetails.getAll(KeyedData.MULTI_DIRECTIONAL);
                    if (!opData.isEmpty()) {
                        Collection<Direction> collection =
                                opData.stream().map(Direction::getRightAngleLeft).collect(Collectors.toSet());
                        ;
                        blockDetails.set(KeyedData.MULTI_DIRECTIONAL, collection);
                    }
                    return;
                }
                DirectionalData directionalData = opDirectional.get();
                Direction direction = directionalData.getDirection().getRightAngleLeft();
                try {
                    directionalData.setDirection(direction);
                } catch (Exception directionNotSupported) {
                    directionNotSupported.printStackTrace();
                }
            });
            this.move(vessel, context, exception);
        }

    }

    public static class RotateRightAroundPosition extends Movement {

        private RotateRightAroundPosition() {

        }

        public void move(Vessel vessel, SyncBlockPosition rotateAround, MovementContext context,
                Consumer<? super Throwable> exception) {
            MovingBlockSet set = new MovingBlockSet();
            vessel.getStructure().getPositions().forEach(s -> {
                MovingBlock block = new SetMovingBlock(s, s).rotateRight(rotateAround);
                set.add(block);
            });
            context.setMovingStructure(set);
            context.setStrictMovement(true);
            context.setMidMovementProcess(mb -> {
                BlockDetails blockDetails = mb.getStoredBlockData();
                Optional<DirectionalData> opDirectional = blockDetails.getDirectionalData();
                if (!(opDirectional.isPresent())) {
                    Collection<Direction> opData = blockDetails.getAll(KeyedData.MULTI_DIRECTIONAL);
                    blockDetails.set(KeyedData.MULTI_DIRECTIONAL,
                            opData.stream().map(Direction::getRightAngleRight).collect(Collectors.toSet()));
                    return;
                }
                DirectionalData directionalData = opDirectional.get();
                Direction direction = directionalData.getDirection().getRightAngleRight();
                try {
                    directionalData.setDirection(direction);
                } catch (DirectionNotSupported directionNotSupported) {
                    directionNotSupported.printStackTrace();
                }
            });
            this.move(vessel, context, exception);
        }

    }

    public static class TeleportToPosition extends Movement {

        private TeleportToPosition() {

        }

        public void move(Vessel vessel, SyncBlockPosition to, MovementContext context,
                Consumer<? super Throwable> exception) {
            MovingBlockSet set = new MovingBlockSet();
            PositionableShipsStructure pss = vessel.getStructure();
            pss.getRelativePositions().forEach(f -> {
                SyncBlockPosition vp = pss.getPosition().getRelative(f);
                SyncBlockPosition vp2 = to.getRelative(f);
                set.add(new SetMovingBlock(vp, vp2));
            });
            context.setMovingStructure(set);
            context.setStrictMovement(true);
            this.move(vessel, context, exception);
        }

    }

    public static class AddToPosition extends Movement {

        private AddToPosition() {
        }

        public void move(Vessel vessel, int x, int y, int z, MovementContext context,
                Consumer<? super Throwable> exception) {
            this.move(vessel, Vector3.valueOf(x, y, z), context, exception);
        }

        public void move(Vessel vessel, Vector3<Integer> addTo, MovementContext context,
                Consumer<? super Throwable> exception) {
            MovingBlockSet set = new MovingBlockSet();
            PositionableShipsStructure pss = vessel.getStructure();
            pss.getRelativePositions().forEach(f -> {
                SyncBlockPosition vp = pss.getPosition().getRelative(f);
                SyncBlockPosition vp2 = vp.getRelative(addTo);
                set.add(new SetMovingBlock(vp, vp2));
            });
            context.setMovingStructure(set);
            if (!(addTo.getX() == 0 && addTo.getY() < 0 && addTo.getZ() == 0)) {
                context.setStrictMovement(true);
            }
            this.move(vessel, context, exception);
        }

    }

}
