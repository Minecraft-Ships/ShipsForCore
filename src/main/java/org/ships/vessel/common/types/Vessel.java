package org.ships.vessel.common.types;

import org.core.entity.LiveEntity;
import org.core.entity.living.human.player.LivePlayer;
import org.core.utils.Bounds;
import org.core.utils.MathUtils;
import org.core.vector.type.Vector2;
import org.core.vector.type.Vector3;
import org.core.world.Extent;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.Positionable;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.async.ASyncBlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.exceptions.NoLicencePresent;
import org.ships.movement.instruction.details.MovementDetails;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface Vessel extends Positionable<BlockPosition> {

    @NotNull String getName() throws NoLicencePresent;

    @NotNull PositionableShipsStructure getStructure();

    void setStructure(@NotNull PositionableShipsStructure pss);

    CompletableFuture<PositionableShipsStructure> updateStructure(OvertimeBlockFinderUpdate finder);

    default CompletableFuture<PositionableShipsStructure> updateStructure() {
        return updateStructure((currentStructure, block) -> OvertimeBlockFinderUpdate.BlockFindControl.USE);
    }


    @NotNull ShipType<? extends Vessel> getType();

    <T extends VesselFlag<?>> @NotNull Optional<T> get(@NotNull Class<T> clazz);

    <T> @NotNull Vessel set(@NotNull Class<? extends VesselFlag<T>> flag, T value);

    @NotNull Vessel set(@NotNull VesselFlag<?> flag);

    int getMaxSpeed();

    @NotNull Vessel setMaxSpeed(@Nullable Integer speed);

    boolean isMaxSpeedSpecified();

    int getAltitudeSpeed();

    @NotNull Vessel setAltitudeSpeed(@Nullable Integer speed);

    boolean isAltitudeSpeedSpecified();

    void moveTowards(int x, int y, int z, @NotNull MovementDetails details);

    void moveTowards(@NotNull Vector3<Integer> vector, @NotNull MovementDetails details);

    void moveTo(@NotNull BlockPosition location, @NotNull MovementDetails details);

    void rotateRightAround(@NotNull BlockPosition location, @NotNull MovementDetails details);

    void rotateLeftAround(@NotNull BlockPosition location, @NotNull MovementDetails details);

    boolean isLoading();

    void setLoading(boolean check);

    void save();

    Optional<String> getCachedName();

    @Override
    default SyncBlockPosition getPosition() {
        return this.getStructure().getPosition();
    }

    default <V, F extends VesselFlag<V>> Optional<V> getValue(Class<F> flagClass) {
        return this.get(flagClass).flatMap(VesselFlag::getValue);
    }

    default Collection<LiveEntity> getEntities() {
        return this.getEntities(e -> true);
    }

    default <X extends LiveEntity> Collection<X> getEntities(Class<X> clazz) {
        return (Collection<X>) this.getEntities(clazz::isInstance);
    }

    default Collection<LiveEntity> getEntities(Predicate<? super LiveEntity> check) {
        Bounds<Integer> bounds = this.getStructure().getBounds();
        bounds.add(1, Integer.MAX_VALUE, 1);
        bounds.add(-1, -1, -1);
        Set<LiveEntity> entities = new HashSet<>();
        this.getStructure().getChunks().stream().map(Extent::getEntities).forEach(entities::addAll);
        entities = entities.stream().filter(e -> {
            Optional<SyncBlockPosition> opTo = e.getAttachedTo();
            return opTo.filter(syncBlockPosition -> bounds.contains(syncBlockPosition.getPosition())).isPresent();
        }).collect(Collectors.toSet());
        Collection<ASyncBlockPosition> blocks = this.getStructure().getAsyncedPositionsRelativeToWorld();
        return entities.stream().filter(check).filter(e -> {
            Optional<SyncBlockPosition> opBlock = e.getAttachedTo();
            //noinspection SuspiciousMethodCalls
            return opBlock.filter(blocks::contains).isPresent();
        }).collect(Collectors.toSet());

    }

    @Deprecated(forRemoval = true)
    default void getEntitiesAsynced(Predicate<? super LiveEntity> predicate,
                                    Consumer<? super Collection<LiveEntity>> output) {
        this.getEntitiesAsynced(predicate, output, Throwable::printStackTrace);
    }

    @Deprecated(forRemoval = true)
    default void getEntitiesAsynced(Predicate<? super LiveEntity> predicate,
                                    Consumer<? super Collection<LiveEntity>> output,
                                    Consumer<Throwable> onException) {
        this.getEntitiesOvertime(predicate, onException).thenAccept(output);
    }

    default CompletableFuture<Collection<LiveEntity>> getEntitiesOvertime(Predicate<? super LiveEntity> predicate) {
        return this.getEntitiesOvertime(predicate, Throwable::printStackTrace);
    }

    default CompletableFuture<Collection<LiveEntity>> getEntitiesOvertime(Predicate<? super LiveEntity> predicate,
                                                                          Consumer<Throwable> onException) {
        return this.getStructure().getChunksAsynced().thenApply(chunks -> {
            try {
                Bounds<Integer> bounds = this.getStructure().getBounds();
                bounds.add(-1, -1, -1);
                bounds.add(1, 5, 1);

                Map<LiveEntity, Vector3<Integer>> entityPositions = chunks
                        .stream()
                        .flatMap(c -> c.getEntities().stream())
                        .collect(Collectors.toMap(e -> e, e -> e
                                .getAttachedTo()
                                .map(Position::getPosition)
                                .orElseGet(() -> e.getPosition().toBlockPosition().getPosition())));

                return entityPositions.entrySet().parallelStream().filter(entry -> {
                    if (entry.getKey() instanceof LivePlayer player) {
                        var position = player.getPosition();
                        var equal = position.toBlockPosition().getPosition().equals(entry.getValue());
                        System.out.println("test");
                    }
                    return bounds.contains(entry.getValue());
                }).map(Map.Entry::getKey).filter(predicate).collect(Collectors.toSet());
            } catch (Throwable e) {
                onException.accept(e);
                return Collections.emptyList();
            }
        });
    }

    default void rotateAnticlockwiseAround(@NotNull BlockPosition location, @NotNull MovementDetails details) {
        this.rotateRightAround(location, details);
    }

    default void rotateClockwiseAround(@NotNull BlockPosition location, @NotNull MovementDetails details) {
        this.rotateLeftAround(location, details);
    }

    default <T> Optional<Integer> getWaterLevel(Function<? super T, ? extends BlockPosition> function,
                                                Collection<T> collection) {
        Map<Vector2<Integer>, Integer> height = new HashMap<>();
        int lowest = Integer.MIN_VALUE;
        Direction[] directions = FourFacingDirection.getFourFacingDirections();
        for (T value : collection) {
            BlockPosition position = function.apply(value);
            for (Direction direction : directions) {
                BlockType type = position.getRelative(direction).getBlockType();
                if (!type.equals(BlockTypes.WATER)) {
                    continue;
                }
                Vector2<Integer> vector = Vector2.valueOf(position.getX() + direction.getAsVector().getX(),
                                                          position.getZ() + direction.getAsVector().getZ());
                if (!height.containsKey(vector)) {
                    height.put(vector, position.getY());
                    continue;
                }
                if (height.getOrDefault(vector, lowest) < position.getY()) {
                    height.replace(vector, position.getY());
                }
            }
        }
        if (height.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(MathUtils.getMostCommonNumber(height.values()));
    }

    default Optional<Integer> getWaterLevel() {
        PositionableShipsStructure pss = this.getStructure();
        return this.getWaterLevel(p -> p, pss.getAsyncedPositionsRelativeToWorld());
    }

}
