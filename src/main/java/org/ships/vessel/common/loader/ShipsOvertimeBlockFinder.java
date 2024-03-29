package org.ships.vessel.common.loader;

import org.core.TranslateCore;
import org.core.schedule.unit.TimeUnit;
import org.core.vector.type.Vector3;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.async.ASyncBlockPosition;
import org.ships.algorthum.blockfinder.BasicBlockFinder;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.config.configuration.ShipsConfig;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Deprecated(forRemoval = true)
public class ShipsOvertimeBlockFinder {

    private final BlockPosition position;
    private final Collection<Vessel> vessels = new HashSet<>();

    public ShipsOvertimeBlockFinder(BlockPosition position) {
        this.position = position;
    }

    public ShipsOvertimeBlockFinder fromVessels(Collection<? extends Vessel> vessels) {
        this.vessels.clear();
        this.vessels.addAll(vessels);
        return this;
    }

    public Collection<Vessel> getVessels() {
        if (this.vessels.isEmpty()) {
            return ShipsPlugin.getPlugin().getVessels();
        }
        return this.vessels;
    }

    @Deprecated(forRemoval = true)
    public void loadOvertime(Consumer<? super Vessel> consumer,
                             Consumer<? super PositionableShipsStructure> exceptionRunner) {
        TranslateCore
                .getScheduleManager()
                .schedule()
                .setAsync(false)
                .setDisplayName("Async vessel finder")
                .setDelay(0)
                .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                .setRunner((sch) -> this.loadOvertime(exceptionRunner).thenAccept(consumer))
                .build(ShipsPlugin.getPlugin())
                .run();
    }

    public CompletableFuture<Vessel> loadOvertime(Consumer<? super PositionableShipsStructure> exceptionRunner) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        Set<Map.Entry<Vector3<Integer>, Vessel>> vessels = this
                .getVessels()
                .parallelStream()
                .collect(Collectors.toMap(v -> v.getPosition().getPosition(), v -> v))
                .entrySet();

        Map.Entry<Byte, Vessel> passed = new AbstractMap.SimpleEntry<>((byte) 0, null);

        if (config.isStructureClickUpdating()) {
            CompletableFuture<Vessel> future = new CompletableFuture<>();
            ShipsPlugin.getPlugin().getVessels().forEach(v -> {
                PositionableShipsStructure pss = v.getStructure();
                Collection<ASyncBlockPosition> collection = pss.getAsyncedPositionsRelativeToWorld();
                TranslateCore
                        .getScheduleManager()
                        .schedule()
                        .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                        .setDelay(0)
                        .setDisplayName("Ship Finder")
                        .setAsync(true)
                        .setRunner((sch) -> {
                            if (collection.parallelStream().anyMatch(p -> p.equals(this.position))) {
                                future.complete(v);
                            }
                        })
                        .build(ShipsPlugin.getPlugin())
                        .run();
            });
            return future;
        }

        BasicBlockFinder finder = ShipsPlugin.getPlugin().getConfig().getDefaultFinder().init();
        return finder.getConnectedBlocksOvertime(this.position, (currentStructure, block) -> {
            Optional<Map.Entry<Vector3<Integer>, Vessel>> opFirst = vessels
                    .parallelStream()
                    .filter(e -> e.getKey().equals(block.getPosition()))
                    .findFirst();
            if (opFirst.isPresent()) {
                passed.setValue(opFirst.get().getValue());
                return OvertimeBlockFinderUpdate.BlockFindControl.USE_AND_FINISH;
            }
            return OvertimeBlockFinderUpdate.BlockFindControl.USE;
        }).thenApply(structure -> {
            if (passed.getValue() == null) {
                exceptionRunner.accept(structure);
            }
            return passed.getValue();
        });
    }

    @Deprecated(forRemoval = true)
    public void loadOvertimeSynced(Consumer<? super Vessel> consumer,
                                   Consumer<? super PositionableShipsStructure> exceptionRunner) {
        loadOvertime(exceptionRunner).thenAccept(consumer);
    }
}
