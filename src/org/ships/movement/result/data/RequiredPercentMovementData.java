package org.ships.movement.result.data;

import org.core.world.position.block.BlockType;

public class RequiredPercentMovementData {

    protected float required;
    protected float has;
    protected BlockType blockType;

    public RequiredPercentMovementData(BlockType blockType, float required, float has){
        this.has = has;
        this.required = required;
        this.blockType = blockType;
    }

    public BlockType getBlockType(){
        return this.blockType;
    }

    public float getRequired(){
        return this.required;
    }

    public float getHas(){
        return this.has;
    }
}
