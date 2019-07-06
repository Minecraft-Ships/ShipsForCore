package org.ships.vessel.common.loader.shipsvessel;

import org.core.CorePlugin;
import org.core.text.TextColours;
import org.core.world.position.BlockPosition;
import org.ships.exceptions.load.FileLoadVesselException;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.vessel.common.loader.ShipsLoader;
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Collection;
import java.util.Optional;

public class ShipsBlockLoader implements ShipsLoader {

    protected BlockPosition position;

    public ShipsBlockLoader(BlockPosition position){
        this.position = position;
    }

    @Override
    public ShipsVessel load() throws LoadVesselException {
        Optional<ShipsVessel> opVessel = ShipsFileLoader.loadAll(e -> {
            if(e instanceof FileLoadVesselException){
                CorePlugin.getConsole().sendMessage(CorePlugin.buildText(TextColours.RED + ((FileLoadVesselException) e).getFile().getPath() + " could not be loaded due to: \n" + e.getReason()));
            }
        }).stream().filter(v -> {
            PositionableShipsStructure pss = v.getStructure();
            Collection<BlockPosition> collection = pss.getPositions();
            return collection.stream().anyMatch(p -> p.equals(this.position));
        }).findAny();
        if(opVessel.isPresent()){
            return opVessel.get();
        }
        throw new LoadVesselException("Block position is not part of a ship: " + this.position.getX() + ", " + this.position.getY() + ", " + this.position.getZ() + ", " + this.position.getWorld().getName());
    }
}
