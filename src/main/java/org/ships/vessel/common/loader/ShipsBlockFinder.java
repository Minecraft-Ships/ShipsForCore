package org.ships.vessel.common.loader;

import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.async.ASyncBlockPosition;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Collection;
import java.util.Optional;

@Deprecated(forRemoval = true)
public class ShipsBlockFinder implements ShipsLoader {

    protected final BlockPosition position;

    public ShipsBlockFinder(BlockPosition position) {
        this.position = position;
    }

    @Override
    public Vessel load() throws LoadVesselException {
        Optional<Vessel> opVessel = ShipsPlugin.getPlugin().getVessels().stream().filter(v -> {
            PositionableShipsStructure pss = v.getStructure();
            Collection<ASyncBlockPosition> collection = pss.getAsyncedPositionsRelativeToWorld();
            return collection.stream().anyMatch(p -> p.getPosition().equals(this.position.getPosition()));
        }).findAny();
        if (opVessel.isPresent()) {
            return opVessel.get();
        }
        throw new LoadVesselException(
                "Block position is not part of a ship: " + this.position.getX() + ", " + this.position.getY() + ", "
                        + this.position.getZ() + ", " + this.position.getWorld().getName());
    }
}
