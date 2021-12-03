package org.ships.vessel.structure;

import org.core.utils.Bounds;
import org.core.vector.type.Vector3;
import org.core.world.ChunkExtent;
import org.core.world.position.Positionable;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.entity.TileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.vessel.sign.ShipsSign;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface PositionableShipsStructure extends ShipsStructure, Positionable<SyncBlockPosition> {

    PositionableShipsStructure setPosition(SyncBlockPosition pos);

    @Deprecated
    PositionableShipsStructure addAir();

    void addAir(Consumer<? super PositionableShipsStructure> onComplete);

    default Bounds<Integer> getBounds() {
        Set<Vector3<Integer>> positions =
                this
                        .getPositionsRelativeTo(Position.toASync(this.getPosition()))
                        .parallelStream()
                        .map(Position::getPosition)
                        .collect(Collectors.toSet());
        if (positions.isEmpty()) {
            throw new IllegalStateException("No structure found");
        }
        Vector3<Integer> randomVector = positions.iterator().next();
        int minX = randomVector.getX();
        int minY = randomVector.getY();
        int minZ = randomVector.getZ();
        int maxX = minX;
        int maxY = minY;
        int maxZ = minZ;
        for (Vector3<Integer> vector : positions) {
            if (minX <= vector.getX()) {
                minX = vector.getX();
            }
            if (minY <= vector.getY()) {
                minY = vector.getY();
            }
            if (minZ <= vector.getZ()) {
                minZ = vector.getZ();
            }
            if (maxX >= vector.getX()) {
                maxX = vector.getX();
            }
            if (maxY >= vector.getY()) {
                maxY = vector.getY();
            }
            if (maxZ >= vector.getZ()) {
                maxZ = vector.getZ();
            }
        }
        return new Bounds<>(Vector3.valueOf(minX, minY, minZ), Vector3.valueOf(maxX, maxY, maxZ));
    }

    default Set<ChunkExtent> getChunks() {
        Set<Vector3<Integer>> vector3Set = this.getPositionsRelativeTo(Position.toASync(this.getPosition())).parallelStream()
                .map(Position::getChunkPosition)
                .collect(Collectors.toUnmodifiableSet());
        return vector3Set.stream()
                .map(pos -> this.getPosition().getWorld().loadChunk(pos))
                .collect(Collectors.toSet());
    }

    default boolean addPosition(BlockPosition position) {
        Vector3<Integer> original = this.getPosition().getPosition();
        Vector3<Integer> next = position.getPosition();
        return this.addPosition(next.minus(original));
    }

    default boolean removePosition(BlockPosition position) {
        Vector3<Integer> original = this.getPosition().getPosition();
        Vector3<Integer> next = position.getPosition();
        return this.removePosition(next.minus(original));
    }

    @Deprecated(forRemoval = true)
    default <T> Collection<T> getAllLike(Function<SyncBlockPosition, ? extends T> function){
        return this.getAllLike(s -> s, function);
    }

    default <P extends BlockPosition, T> Collection<T> getAllLike(Function<? super SyncBlockPosition, P> toPos,
                                                                  Function<P, ?
            extends T> function) {
        return this.getPositions(toPos).stream().map(function).collect(Collectors.toUnmodifiableSet());
    }

    @Deprecated(forRemoval = true)
    default Collection<SyncBlockPosition> getAll(BlockType type) {
        return this.getAll(type, p -> p);
    }

    default <T extends BlockPosition> Collection<T> getAll(BlockType type,
                                                           Function<? super SyncBlockPosition, ? extends T> function) {
        return Collections.unmodifiableCollection(this.getPositions(function).stream()
                .filter(p -> p.getBlockType().equals(type))
                .collect(Collectors.toSet()));
    }

    default Collection<SyncBlockPosition> getAll(Class<? extends TileEntity> class1) {
        return Collections.unmodifiableCollection(this.getPositionsRelativeTo(this).stream()
                .filter(p -> p.getTileEntity().isPresent())
                .filter(p -> class1.isInstance(p.getTileEntity().get()))
                .collect(Collectors.toSet()));
    }

    default Collection<SyncBlockPosition> getAll(ShipsSign sign) {
        return Collections.unmodifiableCollection(this.getPositions().stream()
                .filter(b -> b.getTileEntity().isPresent())
                .filter(b -> b.getTileEntity().get() instanceof LiveSignTileEntity)
                .filter(b -> sign.isSign((SignTileEntity) b.getTileEntity().get()))
                .collect(Collectors.toSet()));
    }

    @Deprecated(forRemoval = true)
    default Collection<SyncBlockPosition> getPositions() {
        return this.getPositionsRelativeTo(this.getPosition());
    }

    default <T extends BlockPosition> Collection<T> getPositions(Function<? super SyncBlockPosition, ? extends T> function) {
        return ShipsStructure.super.getPositionsRelativeTo(function.apply(this.getPosition()));
    }
}
