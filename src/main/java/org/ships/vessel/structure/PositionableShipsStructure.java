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

    Stream<Vector3<Integer>> getOutsideVectorsRelativeToLicence(@NotNull FourFacingDirection direction);

    Stream<SyncBlockPosition> getAir();

    Stream<Vector3<Integer>> getOutsideVectorsRelativeToLicence();

    default Stream<Vector3<Integer>> getOutsideVectorsRelativeToWorld() {
        Vector3<Integer> position = this.getPosition().getPosition();
        return this
                .getOutsideVectorsRelativeToLicence()
                .map(position::plus);
    }

    Bounds<Integer> getBounds();

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

    default Stream<SyncBlockPosition> getPositionsRelativeToPosition(@NotNull BlockPosition pos) {
        WorldExtent world = pos.getWorld();
        return this.getPositionsRelativeToPosition(pos, vector -> (SyncBlockPosition) world.getPosition(vector));
    }

    default Stream<SyncBlockPosition> getPositionsRelativeToWorld(){
        return getPositionsRelativeToPosition(this.getPosition());
    }

    default Stream<SyncBlockPosition> getPositionsRelativeToLicence(){
        return getPositionsRelativeToPosition(this.getPosition().getWorld().getPosition(0, 0, 0));
    }

    boolean addVectorRelativeToLicence(Vector3<Integer> add);

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
        for (Vector3<Integer> vector : this.getVectorsRelativeToLicence().collect(Collectors.toList())) {
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

    default Stream<Vector3<Integer>> getChunkPositions() {
        return this.getPositionsRelativeToPosition(this.getPosition()).parallel().map(Position::getChunkPosition);
    }

    default Stream<ChunkExtent> getLoadedChunks() {
        WorldExtent world = this.getPosition().getWorld();
        return this.getChunkPositions().map(world::getChunk).filter(Optional::isPresent).map(Optional::get);
    }

    default Stream<ChunkExtent> loadChunks() {
        WorldExtent world = this.getPosition().getWorld();
        return this.getChunkPositions().map(world::loadChunk);
    }

    default CompletableFuture<Stream<ChunkExtent>> getChunksAsynced() {
        WorldExtent world = this.getPosition().getWorld();
        CompletableFuture<ChunkExtent>[] futures = this
                .getPositionsRelativeToWorld()
                .map(Position::getChunkPosition)
                .map(world::loadChunkAsynced)
                .toArray(CompletableFuture[]::new);
        return CompletableFuture
                .allOf(futures)
                .thenApply(v -> Stream.of(futures).parallel().map(f -> {
                    try {
                        return f.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException("This should be impossible", e);
                    }
                }));
    }

    boolean addPositionRelativeToWorld(BlockPosition position);

    boolean removePositionRelativeToWorld(BlockPosition position);

    int size();

    <L extends LiveTileEntity> Stream<L> getRelativeToWorld(@NotNull Class<L> class1);

    Stream<LiveSignTileEntity> getRelativeToWorld(@NotNull ShipsSign sign);
}
