package org.ships.movement;

import org.core.CorePlugin;
import org.core.entity.LiveEntity;
import org.core.exceptions.DirectionNotSupported;
import org.core.vector.types.Vector3Int;
import org.core.world.boss.ServerBossBar;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.DirectionalData;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;
import org.ships.config.blocks.BlockListable;
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

    protected void move(Vessel vessel, boolean strict, MovingBlockSet blocks, BasicMovement movement, ServerBossBar bar, MidMovement mid, PostMovement... postMovement) throws MoveException {
        vessel.set(MovingFlag.class, blocks);
        Set<LiveEntity> entities = vessel.getEntities();
        Map<LiveEntity, MovingBlock> entityBlock = new HashMap<>();
        entities.forEach(e -> entityBlock.put(e, blocks.getBefore(e.getPosition().toBlockPosition().getRelative(FourFacingDirection.DOWN)).get()));

        if(bar != null){
            bar.setValue(100);
            bar.setMessage(CorePlugin.buildText("Processing: Pre"));
        }

        VesselMoveEvent.Pre preEvent = new VesselMoveEvent.Pre(vessel, movement, blocks, entityBlock, strict);
        if (CorePlugin.getPlatform().callEvent(preEvent).isCancelled()){
            return;
        }

        if(bar != null){
            bar.setMessage(CorePlugin.buildText("Checking requirements:"));
        }

        Optional<MovingBlock> opLicence = blocks.get(ShipsPlugin.getPlugin().get(LicenceSign.class).get());
        if (!opLicence.isPresent()) {
            vessel.set(MovingFlag.class, null);
            throw new MoveException(new AbstractFailedMovement(vessel, MovementResult.NO_LICENCE_FOUND, null));
        }
        if (vessel instanceof VesselRequirement) {
            try {
                ((VesselRequirement) vessel).meetsRequirements(strict, blocks);
            }catch (Throwable e) {
                vessel.set(MovingFlag.class, null);
                throw e;
            }
        }
        Set<BlockPosition> collided = new HashSet<>();
        blocks.forEach(mb -> {
            if(blocks.stream().anyMatch(mb1 -> mb.getAfterPosition().equals(mb1.getBeforePosition()))){
                return;
            }
            for(BlockType type : vessel.getType().getIgnoredTypes()){
                if(type.equals(mb.getAfterPosition().getBlockType())){
                    return;
                }
            }
            BlockList list = vessel instanceof BlockListable ? ((BlockListable)vessel).getBlockList() : ShipsPlugin.getPlugin().getBlockList();
            BlockInstruction bi = list.getBlockInstruction(mb.getAfterPosition().getBlockType());
            if (!bi.getCollideType().equals(BlockInstruction.CollideType.IGNORE)) {
                collided.add(mb.getAfterPosition());
            }
        });
        if (!collided.isEmpty()) {
            vessel.set(MovingFlag.class, null);
            if(bar != null){
                bar.deregisterPlayers();
            }

            VesselMoveEvent.CollideDetected collideEvent = new VesselMoveEvent.CollideDetected(vessel, movement, blocks, entityBlock, strict, collided);
            CorePlugin.getPlatform().callEvent(collideEvent);

            throw new MoveException(new AbstractFailedMovement(vessel, MovementResult.COLLIDE_DETECTED, collideEvent.getCollisions()));
        }
        try {
            VesselMoveEvent.Main eventMain = new VesselMoveEvent.Main(vessel, movement, blocks, entityBlock, strict);
            if (CorePlugin.getPlatform().callEvent(eventMain).isCancelled()){
                return;
            }
            entities.forEach(e -> e.setGravity(false));
            if(bar != null){
                bar.setMessage(CorePlugin.buildText("Processing: Moving"));
            }
            Result result = movement.move(vessel, blocks, entityBlock, bar, mid, postMovement);
            if(bar != null){
                bar.setMessage(CorePlugin.buildText("Processing: Post movement"));
            }
            VesselMoveEvent.Post eventPost = new VesselMoveEvent.Post(vessel, movement, blocks, entityBlock, strict, result);
            CorePlugin.getPlatform().callEvent(eventPost);

            result.run(vessel, blocks, bar, entityBlock);
        }catch (Throwable e) {
            entities.forEach(entity -> entity.setGravity(true));
            if(bar != null){
                bar.deregisterPlayers();
            }
            vessel.set(MovingFlag.class, null);
            throw e;
        }
    }

    public static class RotateLeftAroundPosition extends Movement {

        private RotateLeftAroundPosition(){

        }

        public void move(Vessel vessel, BlockPosition rotateAround, BasicMovement movement, ServerBossBar bar) throws MoveException {
            MovingBlockSet set = new MovingBlockSet();
            vessel.getStructure().getPositions().forEach(s -> {
                MovingBlock block = new SetMovingBlock(s, s).rotateLeft(rotateAround);
                set.add(block);
            });
            move(vessel, true, set, movement, bar, (mb) -> {
                BlockDetails blockDetails = mb.getStoredBlockData();
                Optional<DirectionalData> opDirectional = blockDetails.getDirectionalData();
                if(!(opDirectional.isPresent())){
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
        }

    }

    public static class RotateRightAroundPosition extends Movement {

        private RotateRightAroundPosition(){

        }

        public void move(Vessel vessel, BlockPosition rotateAround, BasicMovement movement, ServerBossBar bar) throws MoveException{
            MovingBlockSet set = new MovingBlockSet();
            vessel.getStructure().getPositions().forEach(s -> {
                MovingBlock block = new SetMovingBlock(s, s).rotateRight(rotateAround);
                set.add(block);
            });
            move(vessel, true, set, movement, bar, (mb) -> {
                BlockDetails blockDetails = mb.getStoredBlockData();
                Optional<DirectionalData> opDirectional = blockDetails.getDirectionalData();
                if(!(opDirectional.isPresent())){
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

        }

    }

    public static class TeleportToPosition extends Movement {

        private TeleportToPosition(){

        }

        public void move(Vessel vessel, BlockPosition to, BasicMovement movement, ServerBossBar bar) throws MoveException{
            MovingBlockSet set = new MovingBlockSet();
            PositionableShipsStructure pss = vessel.getStructure();
            pss.getRelativePositions().forEach(f -> {
                BlockPosition vp = pss.getPosition().getRelative(f);
                BlockPosition vp2 = to.getRelative(f);
                set.add(new SetMovingBlock(vp, vp2));
            });
            move(vessel, true, set, movement, bar, (mb) -> {

            });
        }

    }

    public static class AddToPosition extends Movement {

        private AddToPosition(){
        }

        public void move(Vessel vessel, int x, int y, int z, BasicMovement movement, ServerBossBar bar) throws MoveException{
            move(vessel, new Vector3Int(x, y, z), movement, bar);
        }

        public void move(Vessel vessel, Vector3Int addTo, BasicMovement movement, ServerBossBar bar) throws MoveException{
            MovingBlockSet set = new MovingBlockSet();
            PositionableShipsStructure pss = vessel.getStructure();
            pss.getRelativePositions().forEach(f -> {
                BlockPosition vp = pss.getPosition().getRelative(f);
                BlockPosition vp2 = vp.getRelative(addTo);
                set.add(new SetMovingBlock(vp, vp2));
            });
            move(vessel, ((addTo.getX() == 0 && addTo.getY() < 0 && addTo.getZ() == 0) ? false : true), set, movement, bar, (mb) -> {

            });
        }

    }

}
