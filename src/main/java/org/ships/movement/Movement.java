package org.ships.movement;

import org.core.CorePlugin;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.exceptions.DirectionNotSupported;
import org.core.vector.types.Vector3Int;
import org.core.world.boss.ServerBossBar;
import org.core.world.direction.Direction;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.DirectionalData;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;
import org.ships.config.blocks.BlockListable;
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
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;
import java.util.function.Consumer;

public class Movement {

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

    protected void move(Vessel vessel, MovementContext context, Consumer<Throwable> exception) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        if(vessel.isLoading()){
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            exception.accept(new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.VESSEL_STILL_LOADING, null)));
            return;
        }
        Optional<MovementContext> opMoving = vessel.getValue(MovingFlag.class);
        if(opMoving.isPresent()){
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            exception.accept(new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.VESSEL_MOVING_ALREADY, true)));
            return;
        }
        vessel.set(MovingFlag.class, context);
        context.getBar().ifPresent(bar -> bar.setMessage(CorePlugin.buildText("Collecting entities: 0")));
        vessel.getEntitiesOvertime(config.getEntityTrackingLimit(), e -> true, e -> {
            EntitySnapshot<? extends LiveEntity> snapshot = e.createSnapshot();
            if (snapshot == null) {
                return;
            }
            Optional<SyncBlockPosition> opAttached = e.getAttachedTo();
            if(!opAttached.isPresent()){
                return;
            }
            Optional<MovingBlock> mBlock = context.getMovingStructure().getBefore(opAttached.get());
            if(!mBlock.isPresent()){
                return;
            }
            context.getEntities().put(snapshot, mBlock.get());
            context.getBar().ifPresent(bar -> bar.setMessage(CorePlugin.buildText("Collecting entities: " + context.getEntities().size())));
        }, entities -> {
            context.getBar().ifPresent(bar -> {
                bar.setValue(100);
                bar.setMessage(CorePlugin.buildText("Processing: Pre"));
            });
            VesselMoveEvent.Pre preEvent = new VesselMoveEvent.Pre(vessel, context);
            if (CorePlugin.getPlatform().callEvent(preEvent).isCancelled()){
                exception.accept(new IllegalStateException("Pre state was cancelled"));
                return;
            }

            context.getBar().ifPresent(bar -> bar.setMessage(CorePlugin.buildText("Checking requirements:")));

            BlockList blockList;
            if (vessel instanceof BlockListable){
                blockList = ((BlockListable) vessel).getBlockList();
            }else{
                blockList = ShipsPlugin.getPlugin().getBlockList();
            }

            if(config.isMovementRequirementsCheckMaxBlockType()) {
                context.getBar().ifPresent(bar -> bar.setMessage(CorePlugin.buildText("Checking requirements: Block limit check")));
                Map<BlockType, Integer> map = new HashMap<>();
                for(MovingBlock block : context.getMovingStructure()){
                    BlockType type = block.getStoredBlockData().getType();
                    if(map.containsKey(type)){
                        map.replace(type, map.get(type) + 1);
                    }else {
                        map.put(type, 1);
                    }
                }
                for(Map.Entry<BlockType, Integer> entry : map.entrySet()){
                    int limit = blockList.getBlockInstruction(entry.getKey()).getBlockLimit();
                    if(limit != -1 && entry.getValue() > limit){
                        vessel.set(MovingFlag.class, null);
                        context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                        exception.accept(new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.TOO_MANY_OF_BLOCK, entry.getKey())));
                        return;
                    }
                }
                /*for (BlockType type : context.getMovingStructure().to(b -> b.getStoredBlockData().getType())) {
                    int limit = blockList.getBlockInstruction(type).getBlockLimit();
                    long count = context.getMovingStructure().stream()
                        .filter(b1 -> b1.getStoredBlockData().getType().equals(type)).count();
                    if (limit != -1 && count > limit) {
                        vessel.set(MovingFlag.class, null);
                        context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                        exception.accept(new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.TOO_MANY_OF_BLOCK, type)));
                        return;
                    }
                }*/
            }

            context.getBar().ifPresent(bar -> bar.setMessage(CorePlugin.buildText("Checking requirements: valid licence")));
            Optional<MovingBlock> opLicence = context.getMovingStructure().get(ShipsPlugin.getPlugin().get(LicenceSign.class).get());
            if (!opLicence.isPresent()) {
                vessel.set(MovingFlag.class, null);
                context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                exception.accept(new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.NO_LICENCE_FOUND, null)));
                return;
            }
            context.getBar().ifPresent(bar -> bar.setMessage(CorePlugin.buildText("Checking requirements: ShipType requirements")));
            if (vessel instanceof VesselRequirement) {
                try {
                    ((VesselRequirement) vessel).meetsRequirements(context);
                }catch (Throwable e) {
                    vessel.set(MovingFlag.class, null);
                    exception.accept(e);
                    return;
                }
            }
            Set<SyncBlockPosition> collided = new HashSet<>();
            context.getBar().ifPresent(bar -> bar.setMessage(CorePlugin.buildText("Checking requirements: Collide")));
            context.getMovingStructure().forEach(mb -> {
                if(context.getMovingStructure().stream().anyMatch(mb1 -> mb.getAfterPosition().equals(mb1.getBeforePosition()))){
                    return;
                }
                for(BlockType type : vessel.getType().getIgnoredTypes()){
                    Optional<SyncBlockPosition> opBlock = mb.getAfterPosition();
                    if(opBlock.isPresent()){
                        if(type.equals(opBlock.get().getBlockType())){
                            return;
                        }
                    }
                }
                BlockList list = vessel instanceof BlockListable ? ((BlockListable)vessel).getBlockList() : ShipsPlugin.getPlugin().getBlockList();
                Optional<SyncBlockPosition> opAfter = mb.getAfterPosition();
                if(opAfter.isPresent()){
                    BlockInstruction bi = list.getBlockInstruction(opAfter.get().getBlockType());
                    if (!bi.getCollideType().equals(BlockInstruction.CollideType.IGNORE)) {
                        collided.add(opAfter.get());
                    }
                }
            });
            if (!collided.isEmpty()) {
                vessel.set(MovingFlag.class, null);
                context.getBar().ifPresent(ServerBossBar::deregisterPlayers);

                VesselMoveEvent.CollideDetected collideEvent = new VesselMoveEvent.CollideDetected(vessel, context, collided);
                CorePlugin.getPlatform().callEvent(collideEvent);

                exception.accept(new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.COLLIDE_DETECTED, collideEvent.getCollisions())));
                return;
            }
            context.getBar().ifPresent(bar -> bar.setMessage(CorePlugin.buildText("Processing requirements:")));
            if(vessel instanceof VesselRequirement){
                VesselRequirement requirement = (VesselRequirement)vessel;
                try {
                    requirement.processRequirements(context);
                } catch (MoveException e) {
                    exception.accept(e);
                    return;
                }
            }
            try {
                VesselMoveEvent.Main eventMain = new VesselMoveEvent.Main(vessel, context);
                if (CorePlugin.getPlatform().callEvent(eventMain).isCancelled()){
                    return;
                }
                entities.forEach(e -> e.setGravity(false));
                context.getBar().ifPresent(b -> b.setMessage(CorePlugin.buildText("Processing: Moving")));
                Result result = context.getMovement().move(vessel, context);
                context.getBar().ifPresent(b -> b.setMessage(CorePlugin.buildText("Processing: Post Moving")));

                result.run(vessel, context);
            }catch (Throwable e) {
                entities.forEach(entity -> entity.setGravity(true));
                context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                vessel.set(MovingFlag.class, null);
                exception.accept(e);
            }
        });
    }

    public static class RotateLeftAroundPosition extends Movement {

        private RotateLeftAroundPosition(){

        }

        public void move(Vessel vessel, SyncBlockPosition rotateAround, MovementContext context, Consumer<Throwable> exception) {
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
                if(!(opDirectional.isPresent())){
                    Optional<Collection<Direction>> opData = blockDetails.get(KeyedData.MULTI_DIRECTIONAL);
                    Collection<Direction> collection = new HashSet<>();
                    if(opData.isPresent()) {
                        opData.get().forEach(d -> collection.add(d.getRightAngleRight()));
                        blockDetails.set(KeyedData.MULTI_DIRECTIONAL, collection);
                    }
                    return;
                }
                DirectionalData directionalData = opDirectional.get();
                Direction direction = directionalData.getDirection().getRightAngleLeft();
                try {
                    directionalData.setDirection(direction);
                } catch (DirectionNotSupported directionNotSupported) {
                    directionNotSupported.printStackTrace();
                }
            });
            move(vessel, context, exception);
        }

    }

    public static class RotateRightAroundPosition extends Movement {

        private RotateRightAroundPosition(){

        }

        public void move(Vessel vessel, SyncBlockPosition rotateAround, MovementContext context, Consumer<Throwable> exception){
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
                if(!(opDirectional.isPresent())){
                    Optional<Collection<Direction>> opData = blockDetails.get(KeyedData.MULTI_DIRECTIONAL);
                    Collection<Direction> collection = new HashSet<>();
                    if(opData.isPresent()) {
                        opData.get().forEach(d -> collection.add(d.getRightAngleRight()));
                        blockDetails.set(KeyedData.MULTI_DIRECTIONAL, collection);
                    }
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
            move(vessel, context, exception);

        }

    }

    public static class TeleportToPosition extends Movement {

        private TeleportToPosition(){

        }

        public void move(Vessel vessel, SyncBlockPosition to, MovementContext context, Consumer<Throwable> exception) {
            MovingBlockSet set = new MovingBlockSet();
            PositionableShipsStructure pss = vessel.getStructure();
            pss.getRelativePositions().forEach(f -> {
                SyncBlockPosition vp = pss.getPosition().getRelative(f);
                SyncBlockPosition vp2 = to.getRelative(f);
                set.add(new SetMovingBlock(vp, vp2));
            });
            context.setMovingStructure(set);
            context.setStrictMovement(true);
            move(vessel, context, exception);
        }

    }

    public static class AddToPosition extends Movement {

        private AddToPosition(){
        }

        public void move(Vessel vessel, int x, int y, int z, MovementContext context, Consumer<Throwable> exception) {
            move(vessel, new Vector3Int(x, y, z), context, exception);
        }

        public void move(Vessel vessel, Vector3Int addTo, MovementContext context, Consumer<Throwable> exception){
            MovingBlockSet set = new MovingBlockSet();
            PositionableShipsStructure pss = vessel.getStructure();
            pss.getRelativePositions().forEach(f -> {
                SyncBlockPosition vp = pss.getPosition().getRelative(f);
                SyncBlockPosition vp2 = vp.getRelative(addTo);
                set.add(new SetMovingBlock(vp, vp2));
            });
            context.setMovingStructure(set);
            if(!(addTo.getX() == 0 && addTo.getY() < 0 && addTo.getZ() == 0)){
                context.setStrictMovement(true);
            }
            move(vessel, context, exception);
        }

    }

}
