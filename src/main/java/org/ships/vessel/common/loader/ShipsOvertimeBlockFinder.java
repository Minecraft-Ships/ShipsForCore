package org.ships.vessel.common.loader;

import org.core.TranslateCore;
import org.core.schedule.unit.TimeUnit;
import org.core.vector.type.Vector3;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.async.ASyncBlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.blockfinder.BasicBlockFinder;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.config.configuration.ShipsConfig;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public void loadOvertime(Consumer<? super Vessel> consumer, Consumer<? super PositionableShipsStructure> exceptionRunner) {
        TranslateCore
                .createSchedulerBuilder()
                .setAsync(true)
                .setDisplayName("Async vessel finder")
                .setDelay(0)
                .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                .setExecutor(() -> this.loadOvertimeSynced(consumer, exceptionRunner)).build(ShipsPlugin.getPlugin()).run();

    }

    public void loadOvertimeSynced(Consumer<? super Vessel> consumer,
                                   Consumer<? super PositionableShipsStructure> exceptionRunner) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        Set<Map.Entry<Vector3<Integer>, Vessel>> vessels = this.vessels
                .parallelStream()
                .collect(Collectors.toMap(v -> v.getPosition().getPosition(), v -> v))
                .entrySet();

        Map.Entry<Byte, Vessel> passed = new AbstractMap.SimpleEntry<>((byte) 0, null);

        if (!config.isStructureAutoUpdating()) {
            ShipsPlugin.getPlugin().getVessels().forEach(v -> {
                PositionableShipsStructure pss = v.getStructure();
                Collection<ASyncBlockPosition> collection = pss
                        .getPositions((Function<SyncBlockPosition, ASyncBlockPosition>) (Position::toASync));
                TranslateCore
                        .createSchedulerBuilder()
                        .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                        .setDelay(0)
                        .setDisplayName("Ship Finder")
                        .setAsync(true)
                        .setExecutor(() -> {
                            if (collection.parallelStream().anyMatch(p -> p.equals(this.position))) {
                                consumer.accept(v);
                            }
                        })
                        .build(ShipsPlugin.getPlugin())
                        .run();
            });
            return;
        }

        BasicBlockFinder finder = ShipsPlugin.getPlugin().getConfig().getDefaultFinder().init();
        finder.getConnectedBlocksOvertime(this.position, new OvertimeBlockFinderUpdate() {
            @Override
            public void onShipsStructureUpdated(@NotNull PositionableShipsStructure structure) {
                if (passed.getValue()==null) {
                    exceptionRunner.accept(structure);
                    return;
                }
                consumer.accept(passed.getValue());
            }

            @Override
            public BlockFindControl onBlockFind(@NotNull PositionableShipsStructure currentStructure, @NotNull BlockPosition block) {
                Optional<Map.Entry<Vector3<Integer>, Vessel>> opFirst = vessels
                        .parallelStream()
                        .filter(e -> e.getKey().equals(block.getPosition()))
                        .findFirst();
                if (opFirst.isPresent()) {
                    passed.setValue(opFirst.get().getValue());
                    return BlockFindControl.USE_AND_FINISH;
                }
                return BlockFindControl.USE;
            }
        });
    }

}
