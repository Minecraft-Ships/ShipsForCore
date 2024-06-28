package org.ships.vessel.structure;

import org.core.utils.Bounds;
import org.core.vector.type.Vector3;
import org.core.world.ChunkExtent;
import org.core.world.WorldExtent;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.Positionable;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.TileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.async.ASyncBlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.vessel.sign.ShipsSign;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface PositionableShipsStructure extends Positionable<SyncBlockPosition> {

    PositionableShipsStructure setPosition(@NotNull SyncBlockPosition pos);

    Collection<Vector3<Integer>> getOutsidePositionsRelativeToCenter(FourFacingDirection direction);

    CompletableFuture<PositionableShipsStructure> fillAir();

    Collection<Vector3<Integer>> getOutsidePositionsRelativeToCenter();

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

    @Deprecated(forRemoval = true)
    default Collection<Vector3<Integer>> getRelativePositionsToCenter() {
        Collection<Vector3<Integer>> originalPositions = new HashSet<>(this.getOriginalRelativeVectorsToCenter());
        originalPositions.add(Vector3.valueOf(0, 0, 0));
        return originalPositions;
    }

    @Deprecated(forRemoval = true)
    Collection<Vector3<Integer>> getOriginalRelativeVectorsToWorld();

    @Deprecated(forRemoval = true)
    Collection<Vector3<Integer>> getOriginalRelativeVectorsToCenter();

    Stream<Vector3<Integer>> getVectorsRelativeTo(@NotNull Vector3<Integer> vector);

    default Stream<Vector3<Integer>> getVectorsRelativeToWorld() {
        return this.getVectorsRelativeTo(this.getPosition().getPosition());
    }

    default Stream<Vector3<Integer>> getVectorsRelativeToLicence() {
        return this.getVectorsRelativeTo(Vector3.valueOf(0, 0, 0));
    }

    default <BlockPos extends BlockPosition> Stream<BlockPos> getPositionsRelativeToPosition(@NotNull BlockPosition pos,
                                                                                             @NotNull Function<Vector3<Integer>, BlockPos> toPos) {
        return this.getVectorsRelativeTo(pos.getPosition()).map(toPos);
    }

    @Deprecated(forRemoval = true)
    default Stream<ASyncBlockPosition> getAsyncPositionsRelativeToPosition(@NotNull BlockPosition pos) {
        WorldExtent world = pos.getWorld();
        return this.getPositionsRelativeToPosition(pos, vector -> (ASyncBlockPosition) world.getAsyncPosition(vector));
    }

    default Stream<SyncBlockPosition> getSyncPositionsRelativeToPosition(@NotNull BlockPosition pos) {
        WorldExtent world = pos.getWorld();
        return this.getPositionsRelativeToPosition(pos, vector -> (SyncBlockPosition) world.getPosition(vector));
    }

    boolean addPositionRelativeToCenter(Vector3<Integer> add);

    boolean removePositionRelativeToCenter(Vector3<Integer> remove);

    void copyFrom(@NotNull PositionableShipsStructure structure);

    boolean matchRelativeToCenter(PositionableShipsStructure structure);

    PositionableShipsStructure clear();

    @Deprecated
    default PositionableShipsStructure setRaw(Collection<? extends Vector3<Integer>> collection) {
        return this.setRawPositionsRelativeToCenter(collection);
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
        for (Vector3<Integer> vector : this.getOriginalRelativeVectorsToCenter()) {
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

    @Deprecated(forRemoval = true)
    default <T extends BlockPosition> Collection<T> getPositionsRelativeTo(Positionable<? extends T> positionable) {
        return this.getPositionsRelativeTo(positionable.getPosition());
    }

    @Deprecated(forRemoval = true)
    default <T extends BlockPosition> Collection<T> getPositionsRelativeTo(T position) {
        WorldExtent world = position.getWorld();
        Function<Vector3<Integer>, T> toType;
        if (position instanceof SyncBlockPosition) {
            toType = vec -> (T) world.getPosition(vec);
        } else {
            toType = vec -> (T) world.getAsyncPosition(vec);
        }
        return this.getPositionsRelativeToPosition(position, toType).collect(Collectors.toSet());
    }

    @Deprecated(forRemoval = true)
    default Set<ChunkExtent> getChunks() {
        Collection<Vector3<Integer>> positions = this
                .getAsyncedPositionsRelativeToWorld()
                .parallelStream()
                .map(Position::getChunkPosition)
                .collect(Collectors.toSet());
        WorldExtent world = this.getPosition().getWorld();
        return positions.stream().map(world::loadChunk).collect(Collectors.toSet());
    }

    default Stream<Vector3<Integer>> getChunkPositions() {
        return this.getAsyncPositionsRelativeToPosition(this.getPosition()).parallel().map(Position::getChunkPosition);
    }

    default Stream<ChunkExtent> getLoadedChunks() {
        WorldExtent world = this.getPosition().getWorld();
        return this.getChunkPositions().map(world::getChunk).filter(Optional::isPresent).map(Optional::get);
    }

    default Stream<ChunkExtent> loadChunks() {
        WorldExtent world = this.getPosition().getWorld();
        return this.getChunkPositions().map(world::loadChunk);
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

    boolean addPositionRelativeToWorld(BlockPosition position);

    boolean removePositionRelativeToWorld(BlockPosition position);

    @Deprecated(forRemoval = true)
    default <P extends BlockPosition, T> Collection<T> getAllLike(Function<? super SyncBlockPosition, P> toPos,
                                                                  Function<P, ? extends T> function) {
        return this.getPositionsRelativeTo(toPos).stream().map(function).collect(Collectors.toUnmodifiableSet());
    }

    @Deprecated(forRemoval = true)
    default <T extends BlockPosition> Collection<T> getAll(BlockType type,
                                                           Function<? super SyncBlockPosition, ? extends T> function) {
        return this
                .getPositionsRelativeTo(function)
                .stream()
                .filter(p -> p.getBlockType().equals(type))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Deprecated(forRemoval = true)
    default Collection<SyncBlockPosition> getAll(Class<? extends TileEntity> class1) {
        return this
                .getRelativeToWorld((Class<? extends LiveTileEntity>) class1)
                .map(LiveTileEntity::getPosition)
                .collect(Collectors.toSet());
    }

    @Deprecated(forRemoval = true)
    default Collection<SyncBlockPosition> getAll(ShipsSign sign) {
        return this.getRelativeToWorld(sign).map(LiveTileEntity::getPosition).collect(Collectors.toSet());
    }

    int size();

    <L extends LiveTileEntity> Stream<L> getRelativeToWorld(@NotNull Class<L> class1);

    Stream<LiveSignTileEntity> getRelativeToWorld(@NotNull ShipsSign sign);

    @Deprecated(forRemoval = true)
    default Collection<SyncBlockPosition> getSyncedPositionsRelativeToWorld() {
        return this.getPositionsRelativeTo(this.getPosition());
    }

    @Deprecated(forRemoval = true)
    default Collection<ASyncBlockPosition> getAsyncedPositionsRelativeToWorld() {
        return this.getPositionsRelativeTo(this.getPosition().toAsyncPosition());
    }

    @Deprecated(forRemoval = true)
    default <T extends BlockPosition> Collection<T> getPositionsRelativeTo(Function<? super SyncBlockPosition, ? extends T> function) {
        return this.getPositionsRelativeTo(function.apply(this.getPosition()));
    }
}
