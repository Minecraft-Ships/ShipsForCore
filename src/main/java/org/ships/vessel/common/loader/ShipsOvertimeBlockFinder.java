package org.ships.vessel.common.loader;

import org.core.vector.type.Vector3;
import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.blockfinder.BasicBlockFinder;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;
import java.util.function.Consumer;
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
        Set<Map.Entry<Vector3<Integer>, Vessel>> vessels = this.vessels
                .stream()
                .collect(Collectors.toMap(v -> v.getPosition().getPosition(), v -> v)).entrySet();

        Map.Entry<Byte, Vessel> passed = new AbstractMap.SimpleEntry<>((byte) 0, null);


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
