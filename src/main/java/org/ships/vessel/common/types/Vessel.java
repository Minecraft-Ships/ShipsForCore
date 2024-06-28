package org.ships.vessel.common.types;

import org.core.entity.LiveEntity;
import org.core.utils.Bounds;
import org.core.utils.MathUtils;
import org.core.vector.type.Vector2;
import org.core.vector.type.Vector3;
import org.core.world.Extent;
import org.core.world.WorldExtent;
import org.core.world.chunk.AsyncChunk;
import org.core.world.chunk.Chunk;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.Positionable;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Vessel extends Positionable<BlockPosition> {

    @NotNull
    String getName() throws NoLicencePresent;

    @NotNull
    PositionableShipsStructure getStructure();

    void setStructure(@NotNull PositionableShipsStructure pss);

    CompletableFuture<PositionableShipsStructure> updateStructure(OvertimeBlockFinderUpdate finder);

    default CompletableFuture<PositionableShipsStructure> updateStructure() {
        return this.updateStructure((currentStructure, block) -> OvertimeBlockFinderUpdate.BlockFindControl.USE);
    }


    @NotNull
    ShipType<? extends Vessel> getType();

    <T extends VesselFlag<?>> @NotNull Optional<T> get(@NotNull Class<T> clazz);

    <T> @NotNull Vessel set(@NotNull Class<? extends VesselFlag<T>> flag, T value);

    @NotNull
    Vessel set(@NotNull VesselFlag<?> flag);

    int getMaxSpeed();

    @NotNull
    Vessel setMaxSpeed(@Nullable Integer speed);

    boolean isMaxSpeedSpecified();

    int getAltitudeSpeed();

    @NotNull
    Vessel setAltitudeSpeed(@Nullable Integer speed);

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

    @Deprecated(forRemoval = true)
    default Collection<LiveEntity> getEntities() {
        return this.getEntities(e -> true);
    }

    @Deprecated(forRemoval = true)
    default <X extends LiveEntity> Collection<X> getEntities(Class<X> clazz) {
        return (Collection<X>) this.getEntities(clazz::isInstance);
    }

    @Deprecated(forRemoval = true)
    default Collection<LiveEntity> getEntities(Predicate<? super LiveEntity> check) {
        return this.getLiveEntities(check).collect(Collectors.toSet());
    }

    default Stream<LiveEntity> getLiveEntities(Predicate<? super LiveEntity> check) {
        Bounds<Integer> bounds = this.getStructure().getBounds();
        bounds.add(1, Integer.MAX_VALUE, 1);
        bounds.add(-1, -1, -1);
        Collection<Vector3<Integer>> blocks = this
                .getStructure()
                .getVectorsRelativeToWorld()
                .collect(Collectors.toList());
        return this.getStructure().getLoadedChunks().flatMap(Extent::getLiveEntities).filter(e -> {
            Optional<SyncBlockPosition> opTo = e.getAttachedTo();
            return opTo.filter(syncBlockPosition -> bounds.contains(syncBlockPosition.getPosition())).isPresent();
        }).filter(check).filter(e -> {
            Optional<SyncBlockPosition> opBlock = e.getAttachedTo();
            //noinspection SuspiciousMethodCalls
            return opBlock.filter(blocks::contains).isPresent();
        });
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

                return chunks
                        .stream()
                        .flatMap(Extent::getLiveEntities)
                        .map(entity -> Map.entry(entity, entity
                                .getAttachedTo()
                                .map(Position::getPosition)
                                .orElseGet(() -> entity.getPosition().toBlockPosition().getPosition())))
                        .filter(entry -> bounds.contains(entry.getValue()))
                        .map(Map.Entry::getKey)
                        .filter(predicate)
                        .collect(Collectors.toSet());
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

    @Deprecated(forRemoval = true)
    default <T> Optional<Integer> getWaterLevel(Function<? super T, ? extends BlockPosition> function,
                                                Collection<T> collection) {
        if (collection.isEmpty()) {
            return Optional.empty();
        }
        WorldExtent world = function.apply(collection.iterator().next()).getWorld();
        List<Vector3<Integer>> positions = collection
                .stream()
                .map(function)
                .map(Position::getPosition)
                .collect(Collectors.toList());

        OptionalInt result = this.getWaterLevel(world, positions);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.getAsInt());
    }

    default OptionalInt getWaterLevel(WorldExtent extent, Collection<Vector3<Integer>> collection) {
        List<AsyncChunk> asyncChunks = extent
                .getChunkExtents()
                .filter(chunk -> collection.stream().anyMatch(vec -> chunk.getBounds().containsWithoutMax(vec)))
                .map(Chunk::createAsync)
                .collect(Collectors.toList());
        Direction[] directions = FourFacingDirection.getFourFacingDirections();
        Map<Vector2<Integer>, Integer> waterLevels = collection
                .stream()
                .flatMap(vec -> Stream.of(directions).map(direction -> vec.plus(direction.getAsVector())))
                .map(vec -> asyncChunks
                        .stream()
                        .filter(chunk -> chunk.getBounds().containsWithoutMax(vec))
                        .findAny()
                        .map(chunk -> Map.entry(vec, chunk.getDetails(vec).getType()))
                        .filter(entry -> entry.getValue().equals(BlockTypes.WATER))
                        .map(Map.Entry::getKey))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(vec -> Vector2.valueOf(vec.getX(), vec.getZ()), Vector3::getY));

        Map<Vector2<Integer>, Integer> filteredWaterLevels = new ConcurrentHashMap<>();
        for (Map.Entry<Vector2<Integer>, Integer> entry : waterLevels.entrySet()) {
            Integer currentWaterLevel = filteredWaterLevels.get(entry.getKey());
            if (currentWaterLevel == null) {
                filteredWaterLevels.put(entry.getKey(), entry.getValue());
                continue;
            }
            if (entry.getValue() > currentWaterLevel) {
                filteredWaterLevels.replace(entry.getKey(), entry.getValue());
            }
        }
        if (filteredWaterLevels.isEmpty()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(MathUtils.getMostCommonNumber(filteredWaterLevels.values()));
    }

    default Optional<Integer> getWaterLevel() {
        PositionableShipsStructure pss = this.getStructure();
        OptionalInt result = this.getWaterLevel(this.getPosition().getWorld(),
                                                pss.getVectorsRelativeToWorld().collect(Collectors.toList()));
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.getAsInt());
    }

}
