package org.ships.vessel.structure;

import org.core.collection.BlockSetSnapshot;
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
import org.core.world.position.impl.sync.SyncPosition;
import org.core.world.structure.StructureBuilder;
import org.ships.vessel.sign.ShipsSign;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface PositionableShipsStructure extends ShipsStructure, Positionable<SyncBlockPosition> {

    PositionableShipsStructure setPosition(SyncBlockPosition pos);

    PositionableShipsStructure addAir();

    default StructureBuilder toCoreStructure(StructureBuilder builder) {
        BlockSetSnapshot structure =
                this
                        .getPositions()
                        .stream()
                        .map(SyncPosition::getBlockDetails)
                        .collect(Collectors.toCollection(BlockSetSnapshot::new));
        return builder.addBlocks(structure);
    }

    default Bounds<Integer> getBounds() {
        Set<Vector3<Integer>> positions = this.getPositions().stream().map(Position::getPosition).collect(Collectors.toSet());
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
        Set<Vector3<Integer>> vector3Set = new HashSet<>();
        this.getPositions().stream()
                .map(Position::getChunkPosition)
                .forEach(vector3Set::add);
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

    default <T> Collection<T> getAllLike(Function<? super SyncBlockPosition, ? extends T> function) {
        Set<T> set = new HashSet<>();
        this.getPositions(this).forEach(b -> set.add(function.apply(b)));
        return Collections.unmodifiableCollection(set);
    }

    default Collection<SyncBlockPosition> getAll(BlockType type) {
        return Collections.unmodifiableCollection(this.getPositions(this).stream()
                .filter(p -> p.getBlockType().equals(type))
                .collect(Collectors.toSet()));
    }

    default Collection<SyncBlockPosition> getAll(Class<? extends TileEntity> class1) {
        return Collections.unmodifiableCollection(this.getPositions(this).stream()
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

    default Collection<SyncBlockPosition> getPositions() {
        return ShipsStructure.super.getPositions(this.getPosition());
    }
}
