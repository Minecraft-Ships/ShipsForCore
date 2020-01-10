package org.ships.config.blocks;

import org.core.world.position.block.BlockType;

public class BlockInstruction {

    protected BlockInstruction.CollideType collideType = CollideType.DETECT_COLLIDE;
    protected int blockLimit = -1;
    protected BlockType type;

    public BlockInstruction(BlockType type){
        this.type = type;
    }

    public BlockType getType(){
        return this.type;
    }

    public int getBlockLimit(){
        return this.blockLimit;
    }

    public BlockInstruction setBlockLimit(int limit){
        this.blockLimit = limit;
        return this;
    }

    public BlockInstruction.CollideType getCollideType(){
        return this.collideType;
    }

    public BlockInstruction setCollideType(BlockInstruction.CollideType collideType){
        this.collideType = collideType;
        return this;
    }

    public enum CollideType {

        DETECT_COLLIDE,
        IGNORE,
        MATERIAL

    }
}
