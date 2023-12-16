package org.ships.vessel.structure;

import org.core.utils.Bounds;
import org.core.vector.type.Vector3;
import org.core.world.ChunkExtent;
import org.core.world.WorldExtent;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.Positionable;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.entity.TileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.async.ASyncBlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.vessel.sign.ShipsSign;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface PositionableShipsStructure extends Positionable<SyncBlockPosition> {

    PositionableShipsStructure setPosition(@NotNull SyncBlockPosition pos);

    Collection<Vector3<Integer>> getOutsidePositionsRelativeToCenter(FourFacingDirection direction);

    @Deprecated(forRemoval = true)
    default Collection<Vector3<Integer>> getOutsideBlocks(@NotNull FourFacingDirection direction) {
        return this.getOutsidePositionsRelativeToCenter(direction);
    }

    @Deprecated(forRemoval = true)
    default void addAir(Consumer<? super PositionableShipsStructure> onComplete) {
        fillAir().thenAccept(onComplete);
    }

    CompletableFuture<PositionableShipsStructure> fillAir();

    default Collection<Vector3<Integer>> getOutsidePositionsRelativeToCenter() {
        return Arrays
                .stream(FourFacingDirection.getFourFacingDirections())
                .flatMap(direction -> this.getOutsideBlocks((FourFacingDirection) direction).stream())
                .collect(Collectors.toSet());
    }

    default Collection<Vector3<Integer>> getOutsidePositionsRelativeToWorld() {
        Vector3<Integer> position = this.getPosition().getPosition();
        return this
                .getOutsidePositionsRelativeToCenter()
                .parallelStream()
                .map(position::plus)
                .collect(Collectors.toSet());
    }

    @Deprecated(forRemoval = true)
    default Collection<Vector3<Integer>> getOutsideBlocks() {
        return this.getOutsidePositionsRelativeToCenter();
    }

    Bounds<Integer> getBounds();

    default Collection<Vector3<Integer>> getRelativePositionsToCenter() {
        Collection<Vector3<Integer>> originalPositions = new HashSet<>(this.getOriginalRelativePositionsToCenter());
        originalPositions.add(Vector3.valueOf(0, 0, 0));
        return originalPositions;
    }

    @Deprecated(forRemoval = true)
    default Collection<Vector3<Integer>> getRelativePositions() {
        return this.getRelativePositionsToCenter();
    }

    Collection<Vector3<Integer>> getOriginalRelativePositionsToCenter();

    @Deprecated(forRemoval = true)
    default Collection<Vector3<Integer>> getOriginalRelativePositions() {
        return this.getOriginalRelativePositionsToCenter();
    }

    @Deprecated(forRemoval = true)
    default boolean addPosition(Vector3<Integer> add) {
        return this.addPositionRelativeToCenter(add);
    }

    boolean addPositionRelativeToCenter(Vector3<Integer> add);

    boolean removePositionRelativeToCenter(Vector3<Integer> remove);

    @Deprecated(forRemoval = true)
    default boolean removePosition(Vector3<Integer> remove) {
        return this.removePositionRelativeToCenter(remove);
    }

    PositionableShipsStructure clear();


    @Deprecated
    default PositionableShipsStructure setRaw(Collection<? extends Vector3<Integer>> collection) {
        return setRawPositionsRelativeToCenter(collection);
    }

    PositionableShipsStructure setRawPositionsRelativeToCenter(Collection<? extends Vector3<Integer>> collection);

    default int getXSize() {
        return this.getSpecificSize(Vector3::getX);
    }

    default int getYSize() {
        return this.getSpecificSize(Vector3::getY);
    }

    default int getZSize() {
        return this.getSpecificSize(Vector3::getZ);
    }

    default int getSpecificSize(Function<? super Vector3<Integer>, Integer> function) {
        Integer min = null;
        Integer max = null;
        for (Vector3<Integer> vector : this.getRelativePositions()) {
            int value = function.apply(vector);
            if (min == null && max == null) {
                max = value;
                min = value;
                continue;
            }
            if (min > value) {
                min = value;
            }
            if (max > value) {
                max = value;
            }
        }
        if (min == 0 && max == 0) {
            return 0;
        }
        return max - min;
    }

    default <T extends BlockPosition> Collection<T> getPositionsRelativeTo(Positionable<? extends T> positionable) {
        return this.getPositionsRelativeTo(positionable.getPosition());
    }

    default <T extends BlockPosition> Collection<T> getPositionsRelativeTo(T position) {
        return this
                .getRelativePositions()
                .stream()
                .map(vector -> (T) position.getRelative(vector))
                .collect(Collectors.toUnmodifiableSet());
    }

    default Set<ChunkExtent> getChunks() {
        Collection<Vector3<Integer>> positions = this
                .getAsyncedPositionsRelativeToWorld()
                .parallelStream()
                .map(Position::getChunkPosition)
                .collect(Collectors.toSet());
        WorldExtent world = this.getPosition().getWorld();
        return positions.stream().map(world::loadChunk).collect(Collectors.toSet());
    }

    default CompletableFuture<Collection<ChunkExtent>> getChunksAsynced() {
        Collection<Vector3<Integer>> positions = this
                .getAsyncedPositionsRelativeToWorld()
                .parallelStream()
                .map(Position::getChunkPosition)
                .collect(Collectors.toSet());
        WorldExtent world = this.getPosition().getWorld();
        Set<CompletableFuture<ChunkExtent>> chunkAsync = positions
                .stream()
                .map(world::loadChunkAsynced)
                .collect(Collectors.toSet());
        return CompletableFuture
                .allOf(chunkAsync.toArray(new CompletableFuture[0]))
                .thenApply(v -> chunkAsync.parallelStream().map(f -> {
                    try {
                        return f.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException("This should be impossible", e);
                    }
                }).collect(Collectors.toSet()));
    }

    @Deprecated(forRemoval = true)
    default boolean addPosition(BlockPosition position) {
        Vector3<Integer> original = this.getPosition().getPosition();
        Vector3<Integer> next = position.getPosition();
        return this.addPosition(next.minus(original));
    }

    default boolean addPositionRelativeToWorld(BlockPosition position) {
        Vector3<Integer> original = this.getPosition().getPosition();
        Vector3<Integer> next = position.getPosition();
        return this.addPosition(next.minus(original));
    }

    @Deprecated(forRemoval = true)
    default boolean removePosition(BlockPosition position) {
        Vector3<Integer> original = this.getPosition().getPosition();
        Vector3<Integer> next = position.getPosition();
        return this.removePosition(next.minus(original));
    }

    default boolean removePositionRelativeToWorld(BlockPosition position) {
        Vector3<Integer> original = this.getPosition().getPosition();
        Vector3<Integer> next = position.getPosition();
        return this.removePosition(next.minus(original));
    }

    default <P extends BlockPosition, T> Collection<T> getAllLike(Function<? super SyncBlockPosition, P> toPos,
                                                                  Function<P, ? extends T> function) {
        return this.getPositions(toPos).stream().map(function).collect(Collectors.toUnmodifiableSet());
    }

    default <T extends BlockPosition> Collection<T> getAll(BlockType type,
                                                           Function<? super SyncBlockPosition, ? extends T> function) {
        return this
                .getPositions(function)
                .stream()
                .filter(p -> p.getBlockType().equals(type))
                .collect(Collectors.toUnmodifiableSet());
    }

    default Collection<SyncBlockPosition> getAll(Class<? extends TileEntity> class1) {
        return this
                .getPositionsRelativeTo(this)
                .stream()
                .filter(p -> p.getTileEntity().isPresent())
                .filter(p -> class1.isInstance(p.getTileEntity().get()))
                .collect(Collectors.toUnmodifiableSet());
    }

    default Collection<SyncBlockPosition> getAll(ShipsSign sign) {
        return this
                .getSyncedPositions()
                .stream()
                .filter(b -> b.getTileEntity().isPresent())
                .filter(b -> b.getTileEntity().get() instanceof LiveSignTileEntity)
                .filter(b -> sign.isSign((SignTileEntity) b.getTileEntity().get()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Deprecated(forRemoval = true)
    default Collection<SyncBlockPosition> getSyncedPositions() {
        return this.getPositionsRelativeTo(this.getPosition());
    }

    default Collection<SyncBlockPosition> getSyncedPositionsRelativeToWorld() {
        return this.getPositionsRelativeTo(this.getPosition());
    }

    @Deprecated(forRemoval = true)
    default Collection<ASyncBlockPosition> getAsyncedPositions() {
        return this.getPositions(Position::toASync);
    }

    default Collection<ASyncBlockPosition> getAsyncedPositionsRelativeToWorld() {
        return this.getPositions(Position::toASync);
    }

    @Deprecated(forRemoval = true)
    default <T extends BlockPosition> Collection<T> getPositions(Function<? super SyncBlockPosition, ? extends T> function) {
        return this.getPositionsRelativeTo(function.apply(this.getPosition()));
    }

    default <T extends BlockPosition> Collection<T> getPositionsRelativeTo(Function<? super SyncBlockPosition, ? extends T> function) {
        return this.getPositionsRelativeTo(function.apply(this.getPosition()));
    }
}
