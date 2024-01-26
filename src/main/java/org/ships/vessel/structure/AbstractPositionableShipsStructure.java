package org.ships.vessel.structure;

import org.core.TranslateCore;
import org.core.exceptions.DirectionNotSupported;
import org.core.schedule.unit.TimeUnit;
import org.core.utils.Bounds;
import org.core.vector.type.Vector3;
import org.core.world.WorldExtent;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.plugin.ShipsPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Collectors;

public class AbstractPositionableShipsStructure implements PositionableShipsStructure {

    private final Collection<Vector3<Integer>> vectors = new LinkedTransferQueue<>();
    private final Collection<Vector3<Integer>> outsideNorth = new LinkedTransferQueue<>();
    private final Collection<Vector3<Integer>> outsideEast = new LinkedTransferQueue<>();
    private final Collection<Vector3<Integer>> outsideSouth = new LinkedTransferQueue<>();
    private final Collection<Vector3<Integer>> outsideWest = new LinkedTransferQueue<>();

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
        return this;
    }

    @Override
    public Collection<Vector3<Integer>> getOutsidePositionsRelativeToCenter(@NotNull FourFacingDirection direction) {
        if (direction.equals(FourFacingDirection.EAST)) {
            return Collections.unmodifiableCollection(this.outsideEast);
        }
        if (direction.equals(FourFacingDirection.WEST)) {
            return Collections.unmodifiableCollection(this.outsideWest);
        }
        if (direction.equals(FourFacingDirection.NORTH)) {
            return Collections.unmodifiableCollection(this.outsideNorth);
        }
        if (direction.equals(FourFacingDirection.SOUTH)) {
            return Collections.unmodifiableCollection(this.outsideSouth);
        }
        throw new RuntimeException("Unknown direction of " + direction.getName());
    }

    @Override
    public CompletableFuture<PositionableShipsStructure> fillAir() {
        CompletableFuture<PositionableShipsStructure> future = new CompletableFuture<>();
        Bounds<Integer> bounds = this.getBounds();
        Vector3<Integer> max = bounds.getIntMax();
        Vector3<Integer> min = bounds.getIntMin();
        WorldExtent world = this.getPosition().getWorld();
        TranslateCore
                .getScheduleManager()
                .schedule()
                .setDisplayName("Air getter")
                .setDelay(0)
                .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                .setAsync(true)
                .setRunner((scheduler -> {
                    for (int x = min.getX(); x < max.getX(); x++) {
                        for (int y = min.getY(); y < max.getY(); y++) {
                            for (int z = min.getZ(); z < max.getZ(); z++) {
                                BlockPosition position = world.getAsyncPosition(x, y, z);
                                if (position.getBlockType().equals(BlockTypes.AIR)) {
                                    this.addPositionRelativeToWorld(position);
                                }
                            }
                        }
                    }
                    TranslateCore
                            .getScheduleManager()
                            .schedule()
                            .setDisplayName("from air getter")
                            .setDelay(0)
                            .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                            .setRunner((s) -> future.complete(this))
                            .build(ShipsPlugin.getPlugin())
                            .run();
                }))
                .build(ShipsPlugin.getPlugin())
                .run();
        return future;
    }

    @Override
    public Collection<Vector3<Integer>> getOutsidePositionsRelativeToCenter() {
        Collection<Vector3<Integer>> vectors = new HashSet<>(this.getOriginalRelativePositionsToCenter());
        if (vectors.stream().noneMatch(v -> v.equals(Vector3.valueOf(0, 0, 0)))) {
            vectors.add(Vector3.valueOf(0, 0, 0));
        }
        return vectors;
    }

    @Override
    public Bounds<Integer> getBounds() {
        if (this.cachedBounds != null) {
            return this.cachedBounds;
        }
        Set<Vector3<Integer>> positions = this
                .getOutsidePositionsRelativeToWorld()
                .parallelStream()
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

    @Override
    public Collection<Vector3<Integer>> getOriginalRelativePositionsToCenter() {
        return this.vectors;
    }

    @Override
    public boolean addPositionRelativeToCenter(Vector3<Integer> add) {
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
        if (structure instanceof AbstractPositionableShipsStructure && this.isEmpty()) {
            AbstractPositionableShipsStructure abstractStructure = (AbstractPositionableShipsStructure) structure;
            this.cachedBounds = abstractStructure.cachedBounds;
            this.vectors.addAll(abstractStructure.vectors);
            this.outsideNorth.addAll(abstractStructure.outsideNorth);
            this.outsideEast.addAll(abstractStructure.outsideEast);
            this.outsideSouth.addAll(abstractStructure.outsideSouth);
            this.outsideWest.addAll(abstractStructure.outsideWest);
            return;
        }
        structure.getRelativePositionsToCenter().parallelStream().forEach(this::addPositionRelativeToCenter);
    }

    @Override
    public boolean matchRelativeToCenter(PositionableShipsStructure structure) {
        return this.getRelativePositionsToCenter().equals(structure.getRelativePositionsToCenter());
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
        collection.forEach(this::addPositionRelativeToCenter);
        return this;
    }

    private void addPositionRelativeToWorld(@NotNull Vector3<Integer> position) {
        Vector3<Integer> original = this.getPosition().getPosition();
        this.addPositionRelativeToCenter(position.minus(original));
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
