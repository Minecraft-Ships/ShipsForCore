package org.ships.config.parsers;

import org.core.configuration.parser.Parser;
import org.core.configuration.parser.StringMapParser;
import org.core.world.position.block.BlockType;
import org.ships.config.blocks.BlockInstruction;

import java.util.*;

public class NodeToBlockInstruction implements StringMapParser<BlockInstruction> {

    private final String COLLIDE_TYPE = "CollideType";
    private final String BLOCK_TYPE = "BlockType";

    @Override
    public Optional<BlockInstruction> parse(Map<String, String> original) {
        String blockType = original.get(this.BLOCK_TYPE);
        String collideType = original.get(this.COLLIDE_TYPE);

        Optional<BlockType> opType = Parser.STRING_TO_BLOCK_TYPE.parse(blockType);
        if(!opType.isPresent()){
            return Optional.empty();
        }
        BlockInstruction bi = new BlockInstruction(opType.get());

        if(collideType != null) {
            ShipsParsers.STRING_TO_COLLIDE_TYPE.parse(collideType).ifPresent(v -> bi.setCollideType(v));
        }
        return Optional.of(bi);
    }

    @Override
    public Map<String, String> unparse(BlockInstruction value) {
        Map<String, String> map = new HashMap<>();
        map.put(this.BLOCK_TYPE, Parser.STRING_TO_BLOCK_TYPE.unparse(value.getType()));
        map.put(this.COLLIDE_TYPE, ShipsParsers.STRING_TO_COLLIDE_TYPE.unparse(value.getCollideType()));
        return map;
    }

    @Override
    public List<String> getKeys() {
        return Arrays.asList(this.BLOCK_TYPE, this.COLLIDE_TYPE);
    }
}





















