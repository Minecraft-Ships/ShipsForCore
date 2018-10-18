package org.ships.config.blocks.parsers;

import org.core.configuration.parser.Parser;
import org.core.configuration.parser.StringMapParser;
import org.core.world.position.block.BlockType;
import org.ships.config.blocks.BlockInstruction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NodeToBlockInstruction implements StringMapParser<BlockInstruction> {

    private final String COLLIDE_TYPE = "CollideType";
    private final String BLOCK_TYPE = "BlockType";

    @Override
    public Optional<BlockInstruction> parse(Map<String, String> original) {
        String blockTypeS = original.get(this.BLOCK_TYPE);
        String collideTypeS = original.get(this.COLLIDE_TYPE);
        if(blockTypeS == null || collideTypeS == null){
            return Optional.empty();
        }
        Optional<BlockType> opType = Parser.STRING_TO_BLOCK_TYPE.parse(blockTypeS);
        if(!opType.isPresent()){
            return Optional.empty();
        }
        BlockInstruction bi = new BlockInstruction(opType.get());
        ShipsParsers.STRING_TO_COLLIDE_TYPE.parse(collideTypeS).ifPresent(v -> bi.setCollideType(v));
        return Optional.of(bi);
    }

    @Override
    public Map<String, String> unparse(BlockInstruction value) {
        Map<String, String> map = new HashMap<>();
        map.put(this.BLOCK_TYPE, Parser.STRING_TO_BLOCK_TYPE.unparse(value.getType()));
        map.put(this.COLLIDE_TYPE, ShipsParsers.STRING_TO_COLLIDE_TYPE.unparse(value.getCollideType()));
        return map;
    }
}





















