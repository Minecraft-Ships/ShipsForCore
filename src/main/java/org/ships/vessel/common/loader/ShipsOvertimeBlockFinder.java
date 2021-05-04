package org.ships.vessel.common.loader;

import org.core.vector.type.Vector3;
import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.blockfinder.BasicBlockFinder;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ShipsOvertimeBlockFinder {

    private final BlockPosition position;

    public ShipsOvertimeBlockFinder(BlockPosition position) {
        this.position = position;
    }

    public void loadOvertime(Consumer<Vessel> consumer, Consumer<PositionableShipsStructure> exceptionRunner) {
        Set<Map.Entry<Vector3<Integer>, Vessel>> vessels = ShipsPlugin
                .getPlugin()
                .getVessels()
                .stream()
                .collect(Collectors.toMap(v -> v.getPosition().getPosition(), v -> v)).entrySet();

        Map.Entry<Byte, Vessel> passed = new AbstractMap.SimpleEntry<>((byte) 0, null);


        BasicBlockFinder finder = ShipsPlugin.getPlugin().getConfig().getDefaultFinder().init();
        finder.getConnectedBlocksOvertime(this.position, new OvertimeBlockFinderUpdate() {
            @Override
            public void onShipsStructureUpdated(@NotNull PositionableShipsStructure structure) {
                if (passed.getValue() == null) {
                    exceptionRunner.accept(structure);
                    return;
                }
                System.out.println("Found: " + structure.getOriginalRelativePositions().size() + " | Structure: " + passed.getValue().getStructure().getOriginalRelativePositions().size());
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
