package org.ships.vessel.common.finder;

import org.core.vector.type.Vector3;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.async.ASyncBlockPosition;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class VesselBlockFinder {

    public static Vessel findCached(BlockPosition position) throws LoadVesselException {
        return ShipsPlugin
                .getPlugin()
                .getVessels()
                .stream()
                .filter(v -> {
                    PositionableShipsStructure pss = v.getStructure();
                    Collection<ASyncBlockPosition> collection = pss.getAsyncedPositionsRelativeToWorld();
                    return collection.parallelStream().anyMatch(p -> p.getPosition().equals(position.getPosition()));
                })
                .findAny()
                .orElseThrow(() -> new LoadVesselException(
                        "Block position is not part of a ship: " + position.getX() + ", " + position.getY() + ", "
                                + position.getZ() + ", " + position.getWorld().getName()));
    }

    public static CompletableFuture<Map.Entry<PositionableShipsStructure, Optional<Vessel>>> findOvertime(BlockPosition position) {
        return findOvertime(position, (pss, pos) -> {});
    }

    public static CompletableFuture<Map.Entry<PositionableShipsStructure, Optional<Vessel>>> findOvertime(BlockPosition position,
                                                                                                          BiConsumer<PositionableShipsStructure, BlockPosition> consumer) {
        Map<Vector3<Integer>, Vessel> map = ShipsPlugin
                .getPlugin()
                .getVessels()
                .parallelStream()
                .filter(vessel -> vessel.getPosition().getWorld().equals(position.getWorld()))
                .collect(Collectors.toMap(vessel -> vessel.getPosition().getPosition(), vessel -> vessel));
        AtomicReference<Vessel> reference = new AtomicReference<>();
        return ShipsPlugin
                .getPlugin()
                .getConfig()
                .getDefaultFinder()
                .getConnectedBlocksOvertime(position, (currentStructure, block) -> {
                    consumer.accept(currentStructure, block);
                    Vector3<Integer> pos = block.getPosition();
                    Vessel vessel = map.get(pos);
                    if (vessel == null) {
                        return OvertimeBlockFinderUpdate.BlockFindControl.USE;
                    }
                    reference.set(vessel);
                    return OvertimeBlockFinderUpdate.BlockFindControl.USE_AND_FINISH;
                })
                .thenApply(pss -> new AbstractMap.SimpleImmutableEntry<>(pss, Optional.ofNullable(reference.get())));
    }

}
