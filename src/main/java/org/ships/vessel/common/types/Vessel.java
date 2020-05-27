package org.ships.vessel.common.types;

import org.core.entity.LiveEntity;
import org.core.vector.types.Vector3Int;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncPosition;
import org.core.world.position.Positionable;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlock;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
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

    void moveTowards(int x, int y, int z, MovementContext context) throws MoveException;
    void moveTowards(Vector3Int vector, MovementContext context) throws MoveException;
    void moveTo(SyncPosition<? extends Number> location, MovementContext context) throws MoveException;
    void rotateRightAround(SyncPosition<? extends Number> location, MovementContext context) throws MoveException;
    void rotateLeftAround(SyncPosition<? extends Number> location, MovementContext context) throws MoveException;

    void setLoading(boolean check);
    boolean isLoading();

    void save();

    @Override
    default SyncBlockPosition getPosition(){
        return getStructure().getPosition();
    }

    default <V, F extends VesselFlag<V>> Optional<V> getValue(Class<F> flagClass){
         Optional<F> opFlag = get(flagClass);
         if (opFlag.isPresent()){
             return opFlag.get().getValue();
         }
         return Optional.empty();
    }

    default Collection<LiveEntity> getEntities(){
        return getEntities(e -> true);
    }

    default <X extends LiveEntity> Collection<X> getEntities(Class<X> clazz){
        return (Collection<X>) getEntities(e -> clazz.isInstance(e));
    }

    default Collection<LiveEntity> getEntities(Predicate<LiveEntity> check){
        Collection<LiveEntity> entities = getPosition().getWorld().getEntities();
        Collection<SyncBlockPosition> blocks = getStructure().getPositions();
        return entities.stream()
                .filter(e -> check.test(e))
                .filter(e -> e.getAttachedTo().isPresent())
                .filter(e -> blocks.stream()
                        .anyMatch(b -> b.equals(e.getAttachedTo().get())))
                .collect(Collectors.toSet());
    }

    default void rotateAnticlockwiseAround(SyncPosition<? extends Number> location, MovementContext context) throws MoveException{
        this.rotateRightAround(location, context);
    }

    default void rotateClockwiseAround(SyncPosition<? extends Number> location, MovementContext context) throws MoveException{
        this.rotateLeftAround(location, context);
    }

    default boolean isInWater(){
        return !getWaterLevel().isPresent();
    }

    default Optional<Integer> getWaterLevel(Collection<MovingBlock> collection, Function<MovingBlock, Optional<SyncBlockPosition>> function){
        int height = -1;
        Direction[] directions = FourFacingDirection.getFourFacingDirections();
        for (MovingBlock mBlock : collection){
            Optional<SyncBlockPosition> opPosition = function.apply(mBlock);
            if(!opPosition.isPresent()){
                continue;
            }
            SyncBlockPosition position = opPosition.get();
            for(Direction direction : directions){
                BlockType type = position.getRelative(direction).getBlockType();
                if(type.equals(BlockTypes.WATER.get())){
                    if(height < position.getY()) {
                        height = position.getY();
                        continue;
                    }
                }
            }
        }
        if(height == -1){
            return Optional.empty();
        }
        return Optional.of(height);
    }

    default Optional<Integer> getWaterLevel(){
        PositionableShipsStructure pss = getStructure();
        Direction[] directions = FourFacingDirection.getFourFacingDirections();
        int height = -1;
        for (SyncBlockPosition position : pss.getPositions()){
            for(Direction direction : directions){
                BlockType type = position.getRelative(direction).getBlockType();
                if(type.equals(BlockTypes.WATER.get())){
                    if(height < position.getY()) {
                        height = position.getY();
                        continue;
                    }
                }
            }
        }
        if(height == -1){
            return Optional.empty();
        }
        return Optional.of(height);
    }

}
