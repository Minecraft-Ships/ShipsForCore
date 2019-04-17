package org.ships.vessel.common.types;

import org.core.CorePlugin;
import org.core.entity.living.human.player.User;
import org.core.vector.types.Vector3Int;
import org.core.world.position.BlockPosition;
import org.core.world.position.ExactPosition;
import org.core.world.position.Position;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.exceptions.MoveException;
import org.ships.exceptions.NoLicencePresent;
import org.ships.movement.Movement;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.sign.LicenceSign;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface ShipsVessel extends Vessel {

    Map<User, CrewPermission> getCrew();
    ExpandedBlockList getBlockList();
    File getFile();
    Map<String, String> getExtraInformation();
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
        LicenceSign licenceSign = ShipsPlugin.getPlugin().get(LicenceSign.class).get();
        if(!licenceSign.isSign(sign)){
            throw new NoLicencePresent(this);
        }
        return sign;
    }

    default ShipsVessel setName(String name) throws NoLicencePresent{
        getSign().setLine(2, CorePlugin.buildText(name));
        return this;
    }

    @Override
    default String getName() {
        try {
            return getSign().getLine(2).get().toPlain();
        } catch (NoLicencePresent e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    default void moveTowards(int x, int y, int z, BasicMovement movement) throws MoveException {
        Movement.MidMovement.ADD_TO_POSITION.move(this, x, y, z, movement);
    }

    @Override
    default void moveTowards(Vector3Int vector, BasicMovement movement) throws MoveException{
        Movement.MidMovement.ADD_TO_POSITION.move(this, vector, movement);
    }

    @Override
    default void moveTo(Position<? extends Number> location, BasicMovement movement) throws MoveException{
        BlockPosition position = location instanceof BlockPosition ? (BlockPosition)location : ((ExactPosition)location).toBlockPosition();
        Movement.MidMovement.TELEPORT_TO_POSITION.move(this, position, movement);
    }

    @Override
    default void rotateRightAround(Position<? extends Number> location, BasicMovement movement) throws MoveException{
        BlockPosition position = location instanceof BlockPosition ? (BlockPosition)location : ((ExactPosition)location).toBlockPosition();
        Movement.MidMovement.ROTATE_RIGHT_AROUND_POSITION.move(this, position, movement);
    }

    @Override
    default void rotateLeftAround(Position<? extends Number> location, BasicMovement movement) throws MoveException{
        BlockPosition position = location instanceof BlockPosition ? (BlockPosition)location : ((ExactPosition)location).toBlockPosition();
        Movement.MidMovement.ROTATE_LEFT_AROUND_POSITION.move(this, position, movement);
    }

    default String getId(){
        return getType().getId() + ":" + getName();
    }
}
