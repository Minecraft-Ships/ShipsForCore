package org.ships.vessel.structure;

import org.core.exceptions.DirectionNotSupported;
import org.core.utils.Bounds;
import org.core.vector.RangeVectorSpliterator;
import org.core.vector.type.Vector3;
import org.core.world.WorldExtent;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.vessel.sign.ShipsSign;

import java.util.*;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AbstractPositionableShipsStructure implements PositionableShipsStructure {

    private final Collection<Vector3<Integer>> vectors = new LinkedTransferQueue<>();
    private final Collection<Vector3<Integer>> outsideNorth = new LinkedTransferQueue<>();
    private final Collection<Vector3<Integer>> outsideEast = new LinkedTransferQueue<>();
    private final Collection<Vector3<Integer>> outsideSouth = new LinkedTransferQueue<>();
    private final Collection<Vector3<Integer>> outsideWest = new LinkedTransferQueue<>();
    private final Collection<Vector3<Integer>> tileEntityVectors = new LinkedTransferQueue<>();

    private SyncBlockPosition position;
    private Bounds<Integer> cachedBounds;


    public AbstractPositionableShipsStructure(SyncBlockPosition position) {
        this.position = position;
    }


    public boolean isEmpty() {
        if (!this.vectors.isEmpty()) {
            return false;
        }
        if (!this.outsideEast.isEmpty()) {
            return false;
        }
        if (!this.outsideNorth.isEmpty()) {
            return false;
        }
        if (!this.outsideSouth.isEmpty()) {
            return false;
        }
        return this.outsideWest.isEmpty();
    }

    @Override
    public SyncBlockPosition getPosition() {
        return this.position;
    }

    @Override
    public PositionableShipsStructure setPosition(@NotNull SyncBlockPosition pos) {
        this.position = pos;
        this.cachedBounds = null;
        return this;
    }

    @Override
    public Stream<Vector3<Integer>> getOutsideVectorsRelativeToLicence(@NotNull FourFacingDirection direction) {
        if (direction.equals(FourFacingDirection.EAST)) {
            return this.outsideEast.stream();
        }
        if (direction.equals(FourFacingDirection.WEST)) {
            return this.outsideWest.stream();
        }
        if (direction.equals(FourFacingDirection.NORTH)) {
            return this.outsideNorth.stream();
        }
        if (direction.equals(FourFacingDirection.SOUTH)) {
            return this.outsideSouth.stream();
        }
        throw new RuntimeException("Unknown direction of " + direction.getName());
    }

    @Override
    public Stream<SyncBlockPosition> getAir() {
        Spliterator<Vector3<Integer>> split = new RangeVectorSpliterator(this.getBounds());
        WorldExtent world = this.position.getWorld();
        return StreamSupport
                .stream(split, false)
                .map(pos -> (SyncBlockPosition) world.getPosition(pos))
                .filter(pos -> pos.getBlockType().equals(BlockTypes.AIR));
    }

    @Override
    public Stream<Vector3<Integer>> getOutsideVectorsRelativeToLicence() {
        Stream<Vector3<Integer>> east = this.outsideEast.stream();
        Stream<Vector3<Integer>> west = this.outsideWest.stream();
        Stream<Vector3<Integer>> north = this.outsideNorth.stream();
        Stream<Vector3<Integer>> south = this.outsideSouth.stream();
        Stream<Vector3<Integer>> result = Stream.concat(east, west);
        result = Stream.concat(north, result);
        result = Stream.concat(south, result);
        return result.distinct();
    }

    @Override
    public Bounds<Integer> getBounds() {
        if (this.cachedBounds != null) {
            return this.cachedBounds;
        }
        Iterator<Vector3<Integer>> positions = this.getOutsideVectorsRelativeToWorld().iterator();
        if (!positions.hasNext()) {
            throw new IllegalStateException("No structure found");
        }
        Vector3<Integer> randomVector = positions.next();
        int minX = randomVector.getX();
        int minY = randomVector.getY();
        int minZ = randomVector.getZ();
        int maxX = minX;
        int maxY = minY;
        int maxZ = minZ;
        while (positions.hasNext()) {
            Vector3<Integer> vector = positions.next();
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

    @Override
    public Stream<Vector3<Integer>> getVectorsRelativeTo(@NotNull Vector3<Integer> vector) {
        Stream<Vector3<Integer>> stream = this.vectors.stream().map(vector::plus);
        return Stream.concat(Stream.of(vector), stream);
    }

    @Override
    public boolean addVectorRelativeToLicence(Vector3<Integer> add) {
        if (this.vectors.parallelStream().anyMatch(v -> v.equals(add))) {
            return false;
        }
        Optional<Vector3<Integer>> opEast = this.outsideEast
                .parallelStream()
                .filter(vector -> vector.getY().equals(add.getY()))
                .filter(vector -> vector.getZ().equals(add.getZ()))
                .findAny();
        if (opEast.isPresent()) {
            int x = opEast.get().getZ();
            if (x < add.getX()) {
                this.outsideEast.remove(opEast.get());
                this.outsideEast.add(add);
                this.cachedBounds = null;
            }
        } else {
            this.outsideEast.add(add);
            this.cachedBounds = null;
        }

        Optional<Vector3<Integer>> opWest = this.outsideWest
                .parallelStream()
                .filter(vector -> vector.getY().equals(add.getY()))
                .filter(vector -> vector.getZ().equals(add.getZ()))
                .findAny();
        if (opWest.isPresent()) {
            int x = opWest.get().getX();
            if (x > add.getX()) {
                this.outsideWest.remove(opWest.get());
                this.outsideWest.add(add);
                this.cachedBounds = null;
            }
        } else {
            this.outsideWest.add(add);
            this.cachedBounds = null;
        }

        Optional<Vector3<Integer>> opNorth = this.outsideNorth
                .parallelStream()
                .filter(vector -> vector.getY().equals(add.getY()))
                .filter(vector -> vector.getX().equals(add.getX()))
                .findAny();
        if (opNorth.isPresent()) {
            int z = opNorth.get().getZ();
            if (z > add.getZ()) {
                this.outsideNorth.remove(opNorth.get());
                this.outsideNorth.add(add);
                this.cachedBounds = null;
            }
        } else {
            this.outsideNorth.add(add);
            this.cachedBounds = null;
        }

        Optional<Vector3<Integer>> opSouth = this.outsideSouth
                .parallelStream()
                .filter(vector -> vector.getY().equals(add.getY()))
                .filter(vector -> vector.getX().equals(add.getX()))
                .findAny();
        if (opSouth.isPresent()) {
            int z = opSouth.get().getZ();
            if (z < add.getX()) {
                this.outsideSouth.remove(opSouth.get());
                this.outsideSouth.add(add);
                this.cachedBounds = null;
            }
        } else {
            this.outsideSouth.add(add);
            this.cachedBounds = null;
        }
        return this.vectors.add(add);
    }

    @Override
    public boolean removePositionRelativeToCenter(@NotNull Vector3<Integer> remove) {
        Vector3<Integer> original = this.getPosition().getPosition();
        Vector3<Integer> next = this.position.getPosition();
        this.cachedBounds = null;
        return this.vectors.remove(next.minus(original));
    }

    @Override
    public void copyFrom(@NotNull PositionableShipsStructure structure) {
        if (structure instanceof AbstractPositionableShipsStructure && this.isEmpty() && this.position.equals(
                structure.getPosition())) {
            AbstractPositionableShipsStructure abstractStructure = (AbstractPositionableShipsStructure) structure;
            this.cachedBounds = abstractStructure.cachedBounds;
            this.vectors.addAll(abstractStructure.vectors);
            this.outsideNorth.addAll(abstractStructure.outsideNorth);
            this.outsideEast.addAll(abstractStructure.outsideEast);
            this.outsideSouth.addAll(abstractStructure.outsideSouth);
            this.outsideWest.addAll(abstractStructure.outsideWest);
            return;
        }
        structure.getVectorsRelativeToWorld().parallel().forEach(this::addPositionRelativeToWorld);
    }

    @Override
    public boolean matchRelativeToCenter(PositionableShipsStructure structure) {
        if (!(structure instanceof AbstractPositionableShipsStructure)) {
            throw new IllegalArgumentException("Structure must be a AbstractPositionableShipsStructure");
        }
        return this.vectors.equals(((AbstractPositionableShipsStructure) structure).vectors);
    }

    @Override
    public @NotNull AbstractPositionableShipsStructure clear() {
        this.vectors.clear();
        this.cachedBounds = null;
        return this;
    }

    @Override
    public PositionableShipsStructure setRawPositionsRelativeToCenter(Collection<? extends Vector3<Integer>> collection) {
        this.clear();
        collection.forEach(this::addVectorRelativeToLicence);
        return this;
    }

    @Override
    public boolean addPositionRelativeToWorld(@NotNull BlockPosition position) {
        if (position instanceof SyncBlockPosition) {
            SyncPosition<Integer> syncedPosition = (SyncPosition<Integer>) position;
            Optional<LiveTileEntity> opTileEntityData = syncedPosition.getTileEntity();
            if (opTileEntityData.isPresent()) {
                Vector3<Integer> original = this.getPosition().getPosition();
                Vector3<Integer> resultPosition = position.getPosition().minus(original);
                this.tileEntityVectors.add(resultPosition);
            }
        }

        return this.addPositionRelativeToWorld(position.getPosition());
    }

    @Override
    public boolean removePositionRelativeToWorld(BlockPosition position) {
        Vector3<Integer> original = this.getPosition().getPosition();
        Vector3<Integer> resultPosition = position.getPosition().minus(original);
        this.tileEntityVectors.remove(resultPosition);
        return this.removePositionRelativeToCenter(resultPosition);
    }

    @Override
    public int size() {
        return this.vectors.size();
    }

    @Override
    public <L extends LiveTileEntity> Stream<L> getRelativeToWorld(@NotNull Class<L> class1) {
        Stream<L> cachedTileEntities = this.tileEntityVectors
                .stream()
                .map(vector -> this.position.getRelative(vector).getTileEntity())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(class1::isInstance)
                .map(lte -> (L) lte);

        Stream<L> allTileEntities = this
                .getPositionsRelativeToWorld()
                .map(SyncPosition::getTileEntity)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(lte -> this.tileEntityVectors.add(
                        lte.getPosition().getPosition().minus(this.position.getPosition())))
                .filter(class1::isInstance)
                .map(lte -> (L) lte);
        return Stream.concat(cachedTileEntities, allTileEntities).distinct();

    }

    @Override
    public Stream<LiveSignTileEntity> getRelativeToWorld(@NotNull ShipsSign sign) {
        return this.getRelativeToWorld(LiveSignTileEntity.class).filter(sign::isSign);
    }

    private boolean addPositionRelativeToWorld(@NotNull Vector3<Integer> position) {
        Vector3<Integer> original = this.getPosition().getPosition();
        return this.addVectorRelativeToLicence(position.minus(original));
    }

    private static Optional<BlockPosition> getNextInLine(Position<Integer> pos,
                                                         Direction direction,
                                                         Collection<? extends BlockPosition> collections)
            throws DirectionNotSupported {
        Vector3<Integer> original = pos.getPosition();
        Collection<Direction> directions = new ArrayList<>(
                Arrays.asList(Direction.withYDirections(FourFacingDirection.getFourFacingDirections())));
        if (!directions.contains(direction)) {
            throw new DirectionNotSupported(direction, "GetNextInLine");
        }
        List<BlockPosition> positions = collections.stream().filter(p -> {
            Vector3<Integer> vector = p.getPosition();
            if (vector.getX().equals(original.getX()) && vector.getY().equals(original.getY())) {
                int oz = original.getZ();
                int vz = vector.getZ();
                if (oz < vz && direction.equals(FourFacingDirection.SOUTH)) {
                    return true;
                } else if (oz > vz && direction.equals(FourFacingDirection.NORTH)) {
                    return true;
                }
            }
            if (vector.getZ().equals(original.getZ()) && vector.getY().equals(original.getY())) {
                int ox = original.getX();
                int vx = vector.getX();
                if (ox < vx && direction.equals(FourFacingDirection.EAST)) {
                    return true;
                } else if (ox > vx && direction.equals(FourFacingDirection.WEST)) {
                    return true;
                }
            }
            if (vector.getX().equals(original.getX()) && vector.getZ().equals(original.getZ())) {
                int oy = original.getY();
                int vy = vector.getY();
                if (oy < vy && direction.equals(FourFacingDirection.UP)) {
                    return true;
                } else {
                    return oy > vy && direction.equals(FourFacingDirection.DOWN);
                }
            }
            return false;
        }).filter(p -> !p.getPosition().equals(original)).collect(Collectors.toList());
        double min = Double.MAX_VALUE;
        BlockPosition current = null;
        for (BlockPosition position : positions) {
            double distance = position.getPosition().distanceSquared(original);
            if (min > distance) {
                min = distance;
                current = position;
            }
        }
        return Optional.ofNullable(current);
    }

}
