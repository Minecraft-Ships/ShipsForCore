package org.ships.vessel.common.types;

import org.core.entity.LiveEntity;
import org.core.vector.types.Vector3Int;
import org.core.world.boss.ServerBossBar;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.BlockPosition;
import org.core.world.position.ExactPosition;
import org.core.world.position.Position;
import org.core.world.position.Positionable;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.exceptions.MoveException;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface Vessel extends Positionable {

    String getName();
    PositionableShipsStructure getStructure();
    void setStructure(PositionableShipsStructure pss);
    ShipType getType();

    <T extends VesselFlag> Optional<T> get(Class<T> clazz);
    <T> Vessel set(Class<? extends VesselFlag<T>> flag, T value);
    Vessel set(VesselFlag<?> flag);

    int getMaxSpeed();
    int getAltitudeSpeed();

    Vessel setMaxSpeed(int speed);
    Vessel setAltitudeSpeed(int speed);

    void moveTowards(int x, int y, int z, BasicMovement movement, ServerBossBar bar) throws MoveException;
    void moveTowards(Vector3Int vector, BasicMovement movement, ServerBossBar bar) throws MoveException;
    void moveTo(Position<? extends Number> location, BasicMovement movement, ServerBossBar bar) throws MoveException;
    void rotateRightAround(Position<? extends Number> location, BasicMovement movement, ServerBossBar bar) throws MoveException;
    void rotateLeftAround(Position<? extends Number> location, BasicMovement movement, ServerBossBar bar) throws MoveException;

    void save();

    @Override
    default BlockPosition getPosition(){
        return getStructure().getPosition();
    }

    default <V, F extends VesselFlag<V>> Optional<V> getValue(Class<F> flagClass){
         Optional<F> opFlag = get(flagClass);
         if (opFlag.isPresent()){
             return opFlag.get().getValue();
         }
         return Optional.empty();
    }

    default Set<LiveEntity> getEntities(){
        BlockPosition position = getPosition();
        Set<LiveEntity> entities = position.getWorld().getEntities();
        PositionableShipsStructure pss = getStructure();
        return entities.stream()
                .filter(e -> pss.getRelativePositions().stream().anyMatch(v -> {
                    BlockPosition shipPosition = position.getRelative(v);
                    ExactPosition entityPosition = e.getPosition();
                    BlockPosition targetPos = entityPosition.toBlockPosition().getRelative(FourFacingDirection.DOWN);
                   return targetPos.equals(shipPosition);
                })).collect(Collectors.toSet());
    }

    default void rotateAnticlockwiseAround(Position<? extends Number> location, BasicMovement movement, ServerBossBar bar) throws MoveException{
        this.rotateRightAround(location, movement, bar);
    }

    default void rotateClockwiseAround(Position<? extends Number> location, BasicMovement movement, ServerBossBar bar) throws MoveException{
        this.rotateLeftAround(location, movement, bar);
    }

}
