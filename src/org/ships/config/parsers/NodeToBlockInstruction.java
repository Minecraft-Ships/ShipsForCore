package org.ships.config.parsers;

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
        Optional<String> opBlockTypeS = original.keySet().stream().filter(p -> p.endsWith(this.BLOCK_TYPE)).findAny();
        if(!opBlockTypeS.isPresent()) {
            return Optional.empty();
        }
        String blockTypeS = original.get(opBlockTypeS.get());
        Optional<BlockType> opType = Parser.STRING_TO_BLOCK_TYPE.parse(blockTypeS);
        if(!opType.isPresent()){
            return Optional.empty();
        }
        BlockInstruction bi = new BlockInstruction(opType.get());

        Optional<String> collideTypeKey = original.keySet().stream().filter(p -> p.endsWith(this.COLLIDE_TYPE)).findAny();
        if(collideTypeKey.isPresent()) {
            String collideTypeS = original.entrySet().stream().filter(e -> e.getKey().equals(collideTypeKey.get())).findAny().get().getValue();
            ShipsParsers.STRING_TO_COLLIDE_TYPE.parse(collideTypeS).ifPresent(v -> bi.setCollideType(v));
        }
        if(bi.getCollideType().equals(BlockInstruction.CollideType.MATERIAL)){
            System.out.println("Parsed: " + bi.getType().getId() + " - " + bi.getCollideType());
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
}





















