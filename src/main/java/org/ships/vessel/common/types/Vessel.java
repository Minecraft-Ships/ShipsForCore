package org.ships.vessel.common.types;

import org.core.CorePlugin;
import org.core.entity.LiveEntity;
import org.core.schedule.Scheduler;
import org.core.vector.types.Vector3Int;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.Positionable;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncPosition;
import org.ships.config.configuration.ShipsConfig;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlock;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface Vessel extends Positionable<BlockPosition> {

    String getName();
    PositionableShipsStructure getStructure();
    void setStructure(PositionableShipsStructure pss);
    ShipType<? extends Vessel> getType();

    <T extends VesselFlag<?>> Optional<T> get(Class<T> clazz);
    <T> Vessel set(Class<? extends VesselFlag<T>> flag, T value);
    Vessel set(VesselFlag<?> flag);

    int getMaxSpeed();
    int getAltitudeSpeed();

    Vessel setMaxSpeed(int speed);
    Vessel setAltitudeSpeed(int speed);

    void moveTowards(int x, int y, int z, MovementContext context, Consumer<Throwable> exception);
    void moveTowards(Vector3Int vector, MovementContext context, Consumer<Throwable> exception);
    void moveTo(SyncPosition<? extends Number> location, MovementContext context, Consumer<Throwable> exception);
    void rotateRightAround(SyncPosition<? extends Number> location, MovementContext context, Consumer<Throwable> exception);
    void rotateLeftAround(SyncPosition<? extends Number> location, MovementContext context, Consumer<Throwable> exception);

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
        return (Collection<X>) getEntities(clazz::isInstance);
    }

    default Collection<LiveEntity> getEntities(Predicate<LiveEntity> check){
        Collection<LiveEntity> entities = getPosition().getWorld().getEntities();
        Collection<SyncBlockPosition> blocks = getStructure().getPositions();
        return entities.stream()
                .filter(check)
                .filter(e -> {
                    Optional<SyncBlockPosition> opBlock = e.getAttachedTo();
                    return opBlock.filter(syncBlockPosition -> blocks.parallelStream().anyMatch(b -> b.equals(syncBlockPosition))).isPresent();
                })
                .collect(Collectors.toSet());

    }

    default void getEntitiesOvertime(int limit, Predicate<LiveEntity> predicate, Consumer<LiveEntity> single, Consumer<Collection<LiveEntity>> output){
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        Set<LiveEntity> entities = new HashSet<>();
        List<LiveEntity> entities2 = new ArrayList<>(getPosition().getWorld().getEntities());
        Scheduler sched = CorePlugin.createSchedulerBuilder().setDisplayName("Ignore").setDelay(0).setExecutor(() -> {}).build(ShipsPlugin.getPlugin());
        int fin = entities2.size() / limit;
        Collection<SyncBlockPosition> pss = getStructure().getPositions();
        for(int A = 0; A < fin; A++){
            final int B = A;
            sched = CorePlugin.createSchedulerBuilder().setDisplayName("entity getter " + A).setDelay(1).setExecutor(() -> {
                int c = (B*limit);
                for(int to = 0; to < limit; to++) {
                    LiveEntity e = entities2.get(c + to);
                    if (!predicate.test(e)) {
                        continue;
                    }
                    Optional<SyncBlockPosition> opPosition = e.getAttachedTo();
                    if (!opPosition.isPresent()) {
                        continue;
                    }
                    if (pss.stream().anyMatch(b -> b.equals(opPosition.get()))) {
                        single.accept(e);
                        entities.add(e);
                    }else if(!e.isOnGround()){
                        SyncBlockPosition bPos = e.getPosition().toBlockPosition();
                        if (pss.stream().noneMatch(b -> bPos.isInLineOfSight(b.getPosition(), FourFacingDirection.DOWN))){
                            continue;
                        }
                        single.accept(e);
                        entities.add(e);
                    }
                }
                if(B == 0){
                    output.accept(entities);
                }
            }).setToRunAfter(sched).build(ShipsPlugin.getPlugin());
        }
        sched.run();
    }

    default void rotateAnticlockwiseAround(SyncPosition<? extends Number> location, MovementContext context, Consumer<Throwable> exception){
        this.rotateRightAround(location, context, exception);
    }

    default void rotateClockwiseAround(SyncPosition<? extends Number> location, MovementContext context, Consumer<Throwable> exception){
        this.rotateLeftAround(location, context, exception);
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
