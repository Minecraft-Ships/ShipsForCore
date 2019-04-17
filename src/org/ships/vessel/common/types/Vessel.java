package org.ships.vessel.common.types;

import org.core.entity.Entity;
import org.core.vector.types.Vector3Int;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.BlockPosition;
import org.core.world.position.ExactPosition;
import org.core.world.position.Position;
import org.core.world.position.Positionable;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.exceptions.MoveException;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Set;
import java.util.stream.Collectors;

public interface Vessel extends Positionable {

    String getName();
    PositionableShipsStructure getStructure();
    void setStructure(PositionableShipsStructure pss);
    ShipType getType();

    int getMaxSpeed();
    int getAltitudeSpeed();

    Vessel setMaxSpeed(int speed);
    Vessel setAltitudeSpeed(int speed);

    void moveTowards(int x, int y, int z, BasicMovement movement) throws MoveException;
    void moveTowards(Vector3Int vector, BasicMovement movement) throws MoveException;
    void moveTo(Position<? extends Number> location, BasicMovement movement) throws MoveException;
    void rotateRightAround(Position<? extends Number> location, BasicMovement movement) throws MoveException;
    void rotateLeftAround(Position<? extends Number> location, BasicMovement movement) throws MoveException;

    void save();

    @Override
    default BlockPosition getPosition(){
        return getStructure().getPosition();
    }

    default Set<Entity> getEntities(){
        BlockPosition position = getPosition();
        Set<Entity> entities = position.getWorld().getEntities();
        PositionableShipsStructure pss = getStructure();
        return entities.stream()
                .filter(e -> pss.getRelativePositions().stream().anyMatch(v -> {
                    BlockPosition shipPosition = position.getRelative(v);
                    ExactPosition entityPosition = e.getPosition();
                    BlockPosition targetPos = entityPosition.toBlockPosition().getRelative(FourFacingDirection.DOWN);
                   return targetPos.equals(shipPosition);
                })).collect(Collectors.toSet());
    }

    default void rotateAnticlockwiseAround(Position<? extends Number> location, BasicMovement movement) throws MoveException{
        this.rotateRightAround(location, movement);
    }

    default void rotateClockwiseAround(Position<? extends Number> location, BasicMovement movement) throws MoveException{
        this.rotateLeftAround(location, movement);
    }

}
