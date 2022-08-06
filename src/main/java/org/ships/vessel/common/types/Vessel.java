package org.ships.vessel.common.types;

import org.core.TranslateCore;
import org.core.entity.LiveEntity;
import org.core.schedule.Scheduler;
import org.core.schedule.unit.TimeUnit;
import org.core.utils.Bounds;
import org.core.utils.MathUtils;
import org.core.vector.type.Vector2;
import org.core.vector.type.Vector3;
import org.core.world.ChunkExtent;
import org.core.world.Extent;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.Positionable;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.async.ASyncBlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.exceptions.NoLicencePresent;
import org.ships.movement.MovementContext;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface Vessel extends Positionable<BlockPosition> {

    @NotNull String getName() throws NoLicencePresent;

    @NotNull PositionableShipsStructure getStructure();

    void setStructure(@NotNull PositionableShipsStructure pss);

    @NotNull ShipType<? extends Vessel> getType();

    <T extends VesselFlag<?>> @NotNull Optional<T> get(@NotNull Class<T> clazz);

    <T> @NotNull Vessel set(@NotNull Class<? extends VesselFlag<T>> flag, T value);

    @NotNull Vessel set(@NotNull VesselFlag<?> flag);

    int getMaxSpeed();

    boolean isMaxSpeedSpecified();

    int getAltitudeSpeed();

    boolean isAltitudeSpeedSpecified();

    Optional<Integer> getMaxSize();

    boolean isMaxSizeSpecified();

    int getMinSize();

    boolean isMinSizeSpecified();

    @NotNull Vessel setMaxSize(@Nullable Integer size);

    @NotNull Vessel setMinSize(@Nullable Integer size);

    @NotNull Vessel setMaxSpeed(@Nullable Integer speed);

    @NotNull Vessel setAltitudeSpeed(@Nullable Integer speed);

    void moveTowards(int x, int y, int z, @NotNull MovementContext context, Consumer<? super Throwable> exception);

    void moveTowards(@NotNull Vector3<Integer> vector, @NotNull MovementContext context, Consumer<? super Throwable> exception);

    void moveTo(@NotNull SyncPosition<? extends Number> location, @NotNull MovementContext context, Consumer<? super Throwable> exception);

    void rotateRightAround(SyncPosition<? extends Number> location, MovementContext context, Consumer<? super Throwable> exception);

    void rotateLeftAround(SyncPosition<? extends Number> location, MovementContext context, Consumer<? super Throwable> exception);

    void setLoading(boolean check);

    boolean isLoading();

    void save();

    Optional<String> getCachedName();

    @Override
    default SyncBlockPosition getPosition() {
        return this.getStructure().getPosition();
    }

    default <V, F extends VesselFlag<V>> Optional<V> getValue(Class<F> flagClass) {
        return this.get(flagClass).flatMap(VesselFlag::getValue);
    }

    default Collection<LiveEntity> getEntities() {
        return this.getEntities(e -> true);
    }

    default <X extends LiveEntity> Collection<X> getEntities(Class<X> clazz) {
        return (Collection<X>) this.getEntities(clazz::isInstance);
    }

    default Collection<LiveEntity> getEntities(Predicate<? super LiveEntity> check) {
        Bounds<Integer> bounds = this.getStructure().getBounds();
        Set<LiveEntity> entities = new HashSet<>();
        this.getStructure().getChunks().stream().map(Extent::getEntities).forEach(entities::addAll);
        entities = entities.stream().filter(e -> {
            Optional<SyncBlockPosition> opTo = e.getAttachedTo();
            return opTo
                    .filter(syncBlockPosition -> bounds.contains(syncBlockPosition.getPosition()))
                    .isPresent();
        }).collect(Collectors.toSet());
        Collection<ASyncBlockPosition> blocks = this.getStructure().getPositions((Function<? super SyncBlockPosition,
                ? extends ASyncBlockPosition>) Position::toASync);
        return entities.stream()
                .filter(check)
                .filter(e -> {
                    Optional<SyncBlockPosition> opBlock = e.getAttachedTo();
                    //noinspection EqualsBetweenInconvertibleTypes
                    return opBlock
                            .filter(syncBlockPosition -> blocks
                                    .parallelStream()
                                    .anyMatch(b -> b.equals(syncBlockPosition)))
                            .isPresent();
                })
                .collect(Collectors.toSet());

    }

    default void getEntitiesOvertime(int limit, Predicate<? super LiveEntity> predicate, Consumer<? super LiveEntity> single, Consumer<? super Collection<LiveEntity>> output) {
        Collection<LiveEntity> entities = new HashSet<>();
        List<LiveEntity> entities2 = new ArrayList<>();
        Set<ChunkExtent> chunks = this.getStructure().getChunks();
        chunks.forEach(c -> entities2.addAll(c.getEntities()));

        Scheduler sched = TranslateCore
                .getScheduleManager()
                .schedule()
                .setDisplayName("Ignore")
                .setDelay(0)
                .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                .setRunner((sch) -> {
                })
                .setAsync(true)
                .build(ShipsPlugin.getPlugin());
        double fin = entities2.size() / (double) limit;
        if (fin != ((int) fin)) {
            fin++;
        }
        if (fin == 0) {
            output.accept(entities);
            return;
        }
        Collection<SyncBlockPosition> pss = this.getStructure().getPositions((Function<? super SyncBlockPosition, ?
                extends SyncBlockPosition>) s -> s);
        for (int A = 0; A < fin; A++) {
            final int B = A;
            sched = TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDisplayName("\tentity getter " + A)
                    .setDelay(1)
                    .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                    .setExecutor(() -> {
                        int c = (B * limit);
                        for (int to = 0; to < limit; to++) {
                            if ((c + to) >= entities2.size()) {
                                break;
                            }
                            LiveEntity e = entities2.get(c + to);
                            if (!predicate.test(e)) {
                                continue;
                            }
                            Optional<SyncBlockPosition> opPosition = e.getAttachedTo();
                            if (!opPosition.isPresent()) {
                                continue;
                            }
                            if (pss.stream().anyMatch(b -> b.equals(opPosition.get()))) {
                                single.accept(e);
                                entities.add(e);
                            } else if (!e.isOnGround()) {
                                SyncBlockPosition bPos = e.getPosition().toBlockPosition();
                                if (pss.stream().noneMatch(b -> bPos.isInLineOfSight(b.getPosition(), FourFacingDirection.DOWN))) {
                                    continue;
                                }
                                single.accept(e);
                                entities.add(e);
                            }
                        }
                        if (B == 0) {
                            output.accept(entities);
                        }
                    })
                    .setToRunAfter(sched)
                    .build(ShipsPlugin.getPlugin());
        }
        sched.run();
    }

    default void rotateAnticlockwiseAround(SyncPosition<? extends Number> location, MovementContext context, Consumer<Throwable> exception) {
        this.rotateRightAround(location, context, exception);
    }

    default void rotateClockwiseAround(SyncPosition<? extends Number> location, MovementContext context, Consumer<Throwable> exception) {
        this.rotateLeftAround(location, context, exception);
    }

    @Deprecated(forRemoval = true)
    default boolean isInWater() {
        return this.getWaterLevel().isEmpty();
    }

    default <T> Optional<Integer> getWaterLevel(Function<? super T, ? extends BlockPosition> function, Collection<T> collection) {
        Map<Vector2<Integer>, Integer> height = new HashMap<>();
        int lowest = Integer.MIN_VALUE;
        Direction[] directions = FourFacingDirection.getFourFacingDirections();
        for (T value : collection) {
            BlockPosition position = function.apply(value);
            for (Direction direction : directions) {
                BlockType type = position.getRelative(direction).getBlockType();
                if (!type.equals(BlockTypes.WATER)) {
                    continue;
                }
                Vector2<Integer> vector = Vector2.valueOf(position.getX() + direction.getAsVector().getX(), position.getZ() + direction.getAsVector().getZ());
                if (!height.containsKey(vector)) {
                    height.put(vector, position.getY());
                    continue;
                }
                if (height.getOrDefault(vector, lowest) < position.getY()) {
                    height.replace(vector, position.getY());
                }
            }
        }
        if (height.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(MathUtils.getMostCommonNumber(height.values()));
    }

    default Optional<Integer> getWaterLevel() {
        PositionableShipsStructure pss = this.getStructure();
        return this.getWaterLevel(p -> p, pss.getPositions((Function<? super SyncBlockPosition, ? extends BlockPosition>) Position::toASync));
    }

}
