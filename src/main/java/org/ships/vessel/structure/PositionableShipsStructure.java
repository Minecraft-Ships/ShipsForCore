package org.ships.vessel.structure;

import org.core.utils.Bounds;
import org.core.vector.type.Vector3;
import org.core.world.ChunkExtent;
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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface PositionableShipsStructure extends Positionable<SyncBlockPosition> {

    PositionableShipsStructure setPosition(@NotNull SyncBlockPosition pos);

    Collection<Vector3<Integer>> getOutsideBlocks(@NotNull FourFacingDirection direction);

    @Deprecated
    PositionableShipsStructure addAir();

    void addAir(Consumer<? super PositionableShipsStructure> onComplete);

    default Collection<Vector3<Integer>> getOutsideBlocks() {
        return Arrays
                .stream(FourFacingDirection.getFourFacingDirections())
                .flatMap(direction -> this.getOutsideBlocks((FourFacingDirection) direction).stream())
                .collect(Collectors.toSet());
    }

    default Bounds<Integer> getBounds() {
        Set<Vector3<Integer>> positions = this.getOutsideBlocks().parallelStream().collect(Collectors.toSet());
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

    Collection<Vector3<Integer>> getRelativePositions();

    Collection<Vector3<Integer>> getOriginalRelativePositions();

    boolean addPosition(Vector3<Integer> add);

    boolean removePosition(Vector3<Integer> remove);

    PositionableShipsStructure clear();

    PositionableShipsStructure setRaw(Collection<? extends Vector3<Integer>> collection);

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
        Collection<ASyncBlockPosition> positions = this.getPositionsRelativeTo(Position.toASync(this.getPosition()));
        Set<Vector3<Integer>> vector3Set = positions
                .parallelStream()
                .map(Position::getChunkPosition)
                .collect(Collectors.toUnmodifiableSet());
        return vector3Set.stream().map(pos -> this.getPosition().getWorld().loadChunk(pos)).collect(Collectors.toSet());
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

    default Collection<SyncBlockPosition> getSyncedPositions() {
        return this.getPositionsRelativeTo(this.getPosition());
    }

    default Collection<ASyncBlockPosition> getAsyncedPositions() {
        return this.getPositions(Position::toASync);
    }

    default <T extends BlockPosition> Collection<T> getPositions(Function<? super SyncBlockPosition, ? extends T> function) {
        return this.getPositionsRelativeTo(function.apply(this.getPosition()));
    }
}
