package org.ships.vessel.structure;

import org.core.TranslateCore;
import org.core.exceptions.DirectionNotSupported;
import org.core.schedule.unit.TimeUnit;
import org.core.vector.type.Vector3;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.async.ASyncBlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.plugin.ShipsPlugin;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AbstractPositionableShipsStructure implements PositionableShipsStructure {

    protected Set<Vector3<Integer>> vectors = new HashSet<>();
    protected SyncBlockPosition position;

    public AbstractPositionableShipsStructure(SyncBlockPosition position) {
        this.position = position;
    }

    @Override
    public SyncBlockPosition getPosition() {
        return this.position;
    }

    @Override
    public PositionableShipsStructure setPosition(SyncBlockPosition pos) {
        this.position = pos;
        return this;
    }

    @Override
    public void addAir(Consumer<? super PositionableShipsStructure> onComplete) {
        Collection<ASyncBlockPosition> positions = this.getPositions((Function<? super SyncBlockPosition, ?
                extends ASyncBlockPosition>) Position::toASync);
        Direction[] directions = FourFacingDirection.getFourFacingDirections();
        TranslateCore.createSchedulerBuilder().setAsync(true)
                .setDisplayName("air check")
                .setDelay(0)
                .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                .setExecutor(() -> {
                    Collection<BlockPosition> toAdd = positions.parallelStream().flatMap(p -> {
                        Collection<BlockPosition> blocksToAdd = new HashSet<>();
                        for (Direction dir : directions) {
                            try {
                                Optional<BlockPosition> nextInLine = getNextInLine(p, dir, positions);
                                if (nextInLine.isEmpty()) {
                                    continue;
                                }
                                BlockPosition p1 = nextInLine.get();
                                BlockPosition target = p;
                                Vector3<Integer> dirV = dir.getAsVector();
                                int disX = p1.getX() - target.getX();
                                int disY = p1.getY() - target.getY();
                                int disZ = p1.getZ() - target.getZ();
                                while (!((disX==0) && (disY==0) && (disZ==0))) {
                                    target = target.getRelative(dir);
                                    if (disX!=0) {
                                        disX = disX - dirV.getX();
                                    }
                                    if (disY!=0) {
                                        disY = disY - dirV.getY();
                                    }
                                    if (disZ!=0) {
                                        disZ = disZ - dirV.getZ();
                                    }
                                    if (!target.getBlockType().equals(BlockTypes.AIR)) {
                                        break;
                                    }
                                    if (blocksToAdd.contains(target)) {
                                        continue;
                                    }
                                    blocksToAdd.add(target);
                                }

                            } catch (DirectionNotSupported directionNotSupported) {
                                directionNotSupported.printStackTrace();
                            }
                        }
                        return blocksToAdd.parallelStream();
                    }).collect(Collectors.toSet());

                    TranslateCore.createSchedulerBuilder()
                            .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                            .setDelay(0)
                            .setDisplayName("combine async")
                            .setAsync(false)
                            .setExecutor(() -> {
                                toAdd.forEach(this::addRawPosition);
                                onComplete.accept(this);
                            })
                            .build(ShipsPlugin.getPlugin());
                }).build(ShipsPlugin.getPlugin());
        positions.forEach(p -> {

        });
    }

    @Override
    public PositionableShipsStructure addAir() {
        Collection<ASyncBlockPosition> positions = this.getPositions((Function<? super SyncBlockPosition, ?
                extends ASyncBlockPosition>) Position::toASync);
        Collection<BlockPosition> toAdd = new ArrayList<>();
        Direction[] directions = FourFacingDirection.getFourFacingDirections();
        positions.forEach(p -> {
            for (Direction dir : directions) {
                try {
                    getNextInLine(p, dir, positions).ifPresent(p1 -> {
                        BlockPosition target = p;
                        Vector3<Integer> dirV = dir.getAsVector();
                        int disX = p1.getX() - target.getX();
                        int disY = p1.getY() - target.getY();
                        int disZ = p1.getZ() - target.getZ();
                        while (!((disX==0) && (disY==0) && (disZ==0))) {
                            target = target.getRelative(dir);
                            if (disX!=0) {
                                disX = disX - dirV.getX();
                            }
                            if (disY!=0) {
                                disY = disY - dirV.getY();
                            }
                            if (disZ!=0) {
                                disZ = disZ - dirV.getZ();
                            }
                            if (!target.getBlockType().equals(BlockTypes.AIR)) {
                                break;
                            }
                            if (toAdd.contains(target)) {
                                continue;
                            }
                            toAdd.add(target);
                        }
                    });
                } catch (DirectionNotSupported directionNotSupported) {
                    directionNotSupported.printStackTrace();
                }
            }
        });
        toAdd.forEach(this::addRawPosition);
        return this;
    }

    @Override
    public Set<Vector3<Integer>> getRelativePositions() {
        Set<Vector3<Integer>> vectors = new HashSet<>(this.getOriginalRelativePositions());
        if (vectors.stream().noneMatch(v -> v.equals(Vector3.valueOf(0, 0, 0)))) {
            vectors.add(Vector3.valueOf(0, 0, 0));
        }
        return vectors;
    }

    @Override
    public Set<Vector3<Integer>> getOriginalRelativePositions() {
        return this.vectors;
    }

    @Override
    public boolean addPosition(Vector3<Integer> add) {
        if (this.vectors.stream().anyMatch(v -> v.equals(add))) {
            return false;
        }
        return this.vectors.add(add);
    }

    private void addRawPosition(Position<Integer> position) {
        Vector3<Integer> original = this.getPosition().getPosition();
        Vector3<Integer> next = position.getPosition();
        this.vectors.add(next.minus(original));
    }

    @Override
    public boolean removePosition(Vector3<Integer> remove) {
        Vector3<Integer> original = this.getPosition().getPosition();
        Vector3<Integer> next = this.position.getPosition();
        return this.vectors.remove(next.minus(original));
    }

    @Override
    public ShipsStructure clear() {
        this.vectors.clear();
        return this;
    }

    @Override
    public ShipsStructure setRaw(Collection<? extends Vector3<Integer>> collection) {
        this.vectors = new HashSet<>(collection);
        return this;
    }

    private static Optional<BlockPosition> getNextInLine(Position<Integer> pos, Direction direction, Collection<? extends BlockPosition> collections) throws DirectionNotSupported {
        Vector3<Integer> original = pos.getPosition();
        Collection<Direction> directions = new ArrayList<>(Arrays.asList(Direction.withYDirections(FourFacingDirection.getFourFacingDirections())));
        if (!directions.contains(direction)) {
            throw new DirectionNotSupported(direction, "");
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
                } else return oy > vy && direction.equals(FourFacingDirection.DOWN);
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
