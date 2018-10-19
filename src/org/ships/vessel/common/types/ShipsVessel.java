package org.ships.vessel.common.types;

import org.core.entity.living.human.player.User;
import org.core.text.TextColours;
import org.core.vector.types.Vector3Int;
import org.core.world.position.BlockPosition;
import org.core.world.position.ExactPosition;
import org.core.world.position.Position;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.exceptions.NoLicencePresent;
import org.ships.movement.Movement;
import org.ships.movement.result.FailedMovement;
import org.ships.permissions.vessel.CrewPermission;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface ShipsVessel extends Vessel {

    Map<User, CrewPermission> getCrew();
    ExpandedBlockList getBlockList();
    File getFile();

    CrewPermission getDefaultPermission();

    default CrewPermission getPermission(User user){
        CrewPermission permission = getCrew().get(user);
        if(permission == null){
            permission = getDefaultPermission();
        }
        return permission;
    }

    default Set<User> getCrew(CrewPermission permission){
        Map<User, CrewPermission> permissionMap = getCrew();
        return permissionMap.keySet().stream().filter(u -> permissionMap.get(u).equals(permission)).collect(Collectors.toSet());
    }

    default LiveSignTileEntity getSign() throws NoLicencePresent{
        Optional<LiveTileEntity> opTile = this.getPosition().getTileEntity();
        if(!opTile.isPresent()){
            throw new NoLicencePresent(this);
        }
        LiveTileEntity tile = opTile.get();
        if(!(tile instanceof LiveSignTileEntity)){
            throw new NoLicencePresent(this);
        }
        LiveSignTileEntity sign = (LiveSignTileEntity)tile;
        if(!sign.getLine(0).equals(TextColours.YELLOW + "[Ships]")){
            throw new NoLicencePresent(this);
        }
        return sign;
    }

    default ShipsVessel setName(String name) throws NoLicencePresent{
        getSign().setLine(2, name);
        return this;
    }

    default String getName() {
        try {
            return getSign().getLine(2);
        } catch (NoLicencePresent e) {
            e.printStackTrace();
        }
        return null;
    }

    default Optional<FailedMovement> moveTowards(int x, int y, int z, BasicMovement movement){
        return Movement.ADD_TO_POSITION.move(this, x, y, z, movement);
    }

    default Optional<FailedMovement> moveTowards(Vector3Int vector, BasicMovement movement){
        return Movement.ADD_TO_POSITION.move(this, vector, movement);
    }

    default Optional<FailedMovement> moveTo(Position<? extends Number> location, BasicMovement movement){
        BlockPosition position = location instanceof BlockPosition ? (BlockPosition)location : ((ExactPosition)location).toBlockPosition();
        return Movement.TELEPORT_TO_POSITION.move(this, position, movement);
    }

    default Optional<FailedMovement> rotateRightAround(Position<? extends Number> location, BasicMovement movement){
        BlockPosition position = location instanceof BlockPosition ? (BlockPosition)location : ((ExactPosition)location).toBlockPosition();
        return Movement.ROTATE_RIGHT_AROUND_POSITION.move(this, position, movement);
    }

    default Optional<FailedMovement> rotateAnticlockwiseAround(Position<? extends Number> location, BasicMovement movement){
        return this.rotateRightAround(location, movement);
    }

    default Optional<FailedMovement> rotateLeftAround(Position<? extends Number> location, BasicMovement movement){
        BlockPosition position = location instanceof BlockPosition ? (BlockPosition)location : ((ExactPosition)location).toBlockPosition();
        return Movement.ROTATE_LEFT_AROUND_POSITION.move(this, position, movement);
    }

    default Optional<FailedMovement> rotateClockwiseAround(Position<? extends Number> location, BasicMovement movement){
        return this.rotateLeftAround(location, movement);
    }

    default String getId(){
        return getType().getId() + ":" + getName();
    }
}
