package org.ships.vessel.common.loader;

import org.core.world.position.BlockPosition;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;
import java.util.Optional;

public class ShipsBlockLoader implements ShipsLoader {

    protected BlockPosition position;

    public ShipsBlockLoader(BlockPosition position){
        this.position = position;
    }

    @Override
    public Vessel load() throws IOException {
        Optional<Vessel> opVessel = ShipsFileLoader.loadAll().stream().filter(v -> v.getStructure().getPositions().stream().anyMatch(p -> p.equals(this.position))).findAny();
        if(opVessel.isPresent()){
            return opVessel.get();
        }
        throw new IOException("Block position is not part of a ship: " + this.position.getX() + ", " + this.position.getY() + ", " + this.position.getZ() + ", " + this.position.getWorld().getName());
    }
}
