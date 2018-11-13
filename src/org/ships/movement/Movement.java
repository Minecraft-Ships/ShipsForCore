package org.ships.movement;

import org.core.entity.Entity;
import org.core.vector.types.Vector3Int;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.TiledBlockDetails;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.container.furnace.FurnaceTileEntity;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.blocks.BlockInstruction;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.FailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.AbstractShipsVessel;
import org.ships.vessel.common.types.ShipsVessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.structure.AbstractPosititionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Movement{

    public static final RotateLeftAroundPosition ROTATE_LEFT_AROUND_POSITION = new RotateLeftAroundPosition();
    public static final RotateRightAroundPosition ROTATE_RIGHT_AROUND_POSITION = new RotateRightAroundPosition();
    public static final AddToPosition ADD_TO_POSITION = new AddToPosition();
    public static final TeleportToPosition TELEPORT_TO_POSITION = new TeleportToPosition();

    protected Optional<FailedMovement> move(ShipsVessel vessel, MovingBlockSet blocks, BasicMovement movement) {
        Set<Entity> entities = vessel.getEntities();
        Map<Entity, MovingBlock> entityBlock = new HashMap<>();
        entities.stream().forEach(e -> e.setGravity(false));
        entities.stream().forEach(e -> entityBlock.put(e, blocks.getBefore(e.getPosition().toBlockPosition().getRelative(FourFacingDirection.DOWN)).get()));
        System.out.println("Move: Size: " + blocks.size());
        Optional<MovingBlock> opLicence = blocks.get(ShipsPlugin.getPlugin().get(LicenceSign.class).get());
        if (!opLicence.isPresent()) {
            System.out.println("No Licence sign");
            return Optional.of(new AbstractFailedMovement(vessel, MovementResult.NO_LICENCE_FOUND));
        }
        if (vessel instanceof AbstractShipsVessel) {
            System.out.println("Vessel is AbstractShipsVessel");
            Optional<FailedMovement> opRequirements = ((AbstractShipsVessel) vessel).meetsRequirement(blocks);
            if (opRequirements.isPresent()) {
                return opRequirements;
            }
        }
        if (blocks.stream().anyMatch(mb -> {
            if(blocks.stream().anyMatch(mb1 -> mb.getAfterPosition().equals(mb1.getBeforePosition()))){
                return false;
            }
            for(BlockType type : vessel.getType().getIgnoredTypes()){
                if(type.equals(mb.getAfterPosition().getBlockType())){
                    return false;
                }
            }
            BlockInstruction bi = vessel.getBlockList().getBlockInstruction(mb.getAfterPosition().getBlockType());
            if (!bi.getCollideType().equals(BlockInstruction.CollideType.IGNORE)) {
                return true;
            }
            return false;
        })) {
            return Optional.of(new AbstractFailedMovement(vessel, MovementResult.COLLIDE_DETECTED));
        }
        System.out.println("Checking Stores 0");
        blocks.stream().forEach(b -> {
            BlockDetails bd = b.getCurrentBlockData();
            if(bd instanceof TiledBlockDetails){
                TileEntitySnapshot tes = ((TiledBlockDetails) bd).getTileEntity();
                if(tes instanceof FurnaceTileEntity){
                    FurnaceTileEntity fte = (FurnaceTileEntity) tes;
                    fte.getInventory().getSlots().stream().forEach(s -> System.out.println("\tSlot: " + s.getPosition().orElse(-1) + " - " + s.getItem().orElse(null)));
                }
            }
        });
        Optional<FailedMovement> opMovement = movement.move(vessel, blocks);
        if (opMovement.isPresent()) {
            return opMovement;
        }
        System.out.println("reposising entities");
        entityBlock.entrySet().stream().forEach(e -> e.getKey().setPosition(e.getValue().getAfterPosition().getRelative(FourFacingDirection.UP)));
        System.out.println("resetting gravity");
        entities.stream().forEach(e -> e.setGravity(true));
        System.out.println("Storing position");
        PositionableShipsStructure pss = new AbstractPosititionableShipsStructure(opLicence.get().getAfterPosition());
        blocks.stream().forEach(m -> pss.addPosition(m.getAfterPosition()));
        System.out.println("Setting position");
        vessel.setStructure(pss);
        System.out.println("Saving");
        vessel.save();
        return Optional.empty();
    }

    public static class RotateLeftAroundPosition extends Movement {

        private RotateLeftAroundPosition(){

        }

        public Optional<FailedMovement> move(ShipsVessel vessel, BlockPosition rotateAround, BasicMovement movement) {
            MovingBlockSet set = new MovingBlockSet();
            vessel.getStructure().getPositions().stream().forEach(s -> {
                MovingBlock block = new SetMovingBlock(s, s).rotateLeft(rotateAround);
                set.add(block);
            });
            return move(vessel, set, movement);
        }

    }

    public static class RotateRightAroundPosition extends Movement {

        private RotateRightAroundPosition(){

        }

        public Optional<FailedMovement> move(ShipsVessel vessel, BlockPosition rotateAround, BasicMovement movement) {
            MovingBlockSet set = new MovingBlockSet();
            vessel.getStructure().getPositions().stream().forEach(s -> {
                MovingBlock block = new SetMovingBlock(s, s).rotateRight(rotateAround);
                set.add(block);
            });
            return move(vessel, set, movement);
        }

    }

    public static class TeleportToPosition extends Movement {

        private TeleportToPosition(){

        }

        public Optional<FailedMovement> move(ShipsVessel vessel, BlockPosition to, BasicMovement movement) {
            MovingBlockSet set = new MovingBlockSet();
            PositionableShipsStructure pss = vessel.getStructure();
            pss.getRelativePositions().stream().forEach(f -> {
                BlockPosition vp = pss.getPosition().getRelative(f);
                BlockPosition vp2 = to.getRelative(f);
                set.add(new SetMovingBlock(vp, vp2));
            });
            return move(vessel, set, movement);
        }

    }

    public static class AddToPosition extends Movement {

        private AddToPosition(){
        }

        public Optional<FailedMovement> move(ShipsVessel vessel, int x, int y, int z, BasicMovement movement){
            return move(vessel, new Vector3Int(x, y, z), movement);
        }

        public Optional<FailedMovement> move(ShipsVessel vessel, Vector3Int addTo, BasicMovement movement){
            MovingBlockSet set = new MovingBlockSet();
            PositionableShipsStructure pss = vessel.getStructure();
            System.out.println("PositionableShipsStructure: " + pss.getRelativePositions().size());
            pss.getRelativePositions().stream().forEach(f -> {
                System.out.println("MovingBlock - ForEach");
                BlockPosition vp = pss.getPosition().getRelative(f);
                BlockPosition vp2 = vp.getRelative(addTo);
                set.add(new SetMovingBlock(vp, vp2));
            });
            System.out.println("MovingBlockSet: " + set.size());
            return move(vessel, set, movement);
        }

    }

}
