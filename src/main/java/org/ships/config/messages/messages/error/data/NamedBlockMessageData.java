package org.ships.config.messages.messages.error.data;

import org.core.world.position.block.BlockType;
import org.ships.vessel.common.types.Vessel;

public class NamedBlockMessageData {

    private String namedBlock;
    private BlockType type;
    private Vessel vessel;

    public String getNamedBlock() {
        if (this.namedBlock == null && this.type != null) {
            return this.type.getName();
        }
        return this.namedBlock;
    }

    public NamedBlockMessageData setNamedBlock(String namedBlock) {
        this.namedBlock = namedBlock;
        return this;
    }

    public BlockType getType() {
        return this.type;
    }

    public NamedBlockMessageData setType(BlockType type) {
        this.type = type;
        return this;
    }

    public Vessel getVessel() {
        return this.vessel;
    }

    public NamedBlockMessageData setVessel(Vessel vessel) {
        this.vessel = vessel;
        return this;
    }
}
