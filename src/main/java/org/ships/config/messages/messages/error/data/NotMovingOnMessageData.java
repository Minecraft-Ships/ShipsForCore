package org.ships.config.messages.messages.error.data;

import org.core.world.position.block.BlockType;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.HashSet;

public class NotMovingOnMessageData {

    private Vessel vessel;
    private Collection<BlockType> moveInMaterials = new HashSet<>();

    public Vessel getVessel() {
        return this.vessel;
    }

    public NotMovingOnMessageData setVessel(Vessel vessel) {
        this.vessel = vessel;
        return this;
    }

    public Collection<BlockType> getMoveInMaterials() {
        return this.moveInMaterials;
    }

    public NotMovingOnMessageData setMoveInMaterials(Collection<BlockType> moveInMaterials) {
        this.moveInMaterials = moveInMaterials;
        return this;
    }
}
