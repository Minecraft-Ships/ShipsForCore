package org.ships.vessel.common.loader;

import org.core.CorePlugin;
import org.core.text.TextColours;
import org.core.world.position.BlockPosition;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.vessel.common.types.AbstractShipsVessel;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Collection;
import java.util.Optional;

public class ShipsBlockLoader implements ShipsLoader {

    protected BlockPosition position;

    public ShipsBlockLoader(BlockPosition position){
        this.position = position;
    }

    @Override
    public Vessel load() throws LoadVesselException {
        Optional<AbstractShipsVessel> opVessel = ShipsFileLoader.loadAll(e -> {
            if(e.getFile().isPresent()){
                CorePlugin.getConsole().sendMessage(CorePlugin.buildText(TextColours.RED + e.getFile().get().getPath() + " could not be loaded due to: \n" + e.getReason()));
            }
        }).stream().filter(v -> {
            PositionableShipsStructure pss = v.getStructure();
            Collection<BlockPosition> collection = pss.getPositions();
            return collection.stream().anyMatch(p -> p.equals(this.position));
        }).findAny();
        if(opVessel.isPresent()){
            return opVessel.get();
        }
        throw new LoadVesselException(null, "Block position is not part of a ship: " + this.position.getX() + ", " + this.position.getY() + ", " + this.position.getZ() + ", " + this.position.getWorld().getName());
    }
}
