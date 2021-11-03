package org.ships.vessel.common.loader;

import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Collection;
import java.util.Optional;

public class ShipsBlockFinder implements ShipsLoader {

    protected final SyncBlockPosition position;

    public ShipsBlockFinder(SyncBlockPosition position) {
        this.position = position;
    }

    @Override
    public Vessel load() throws LoadVesselException {
        Optional<Vessel> opVessel = ShipsPlugin.getPlugin().getVessels().stream().filter(v -> {
            PositionableShipsStructure pss = v.getStructure();
            Collection<SyncBlockPosition> collection = pss.getPositions();
            return collection.stream().anyMatch(p -> p.equals(this.position));
        }).findAny();
        if (opVessel.isPresent()) {
            return opVessel.get();
        }
        throw new LoadVesselException("Block position is not part of a ship: " + this.position.getX() + ", " + this.position.getY() + ", " + this.position.getZ() + ", " + this.position.getWorld().getName());
    }
}
