package org.ships.config.parsers;

import org.core.config.parser.Parser;
import org.core.config.parser.StringMapParser;
import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.blocks.instruction.ModifiableBlockInstruction;

import java.util.*;

public class NodeToBlockInstruction implements StringMapParser<ModifiableBlockInstruction> {

    private static final String COLLIDE_TYPE = "CollideType";
    private static final String BLOCK_TYPE = "BlockType";
    private static final String BLOCK_LIMIT = "BlockLimit";

    @Override
    public Optional<ModifiableBlockInstruction> parse(Map<String, String> original) {
        Optional<Map.Entry<String, String>> opBlockType = original
                .entrySet()
                .stream()
                .filter(e -> e.getKey().equals(BLOCK_TYPE))
                .findAny();
        Optional<Map.Entry<String, String>> opCollideType = original
                .entrySet()
                .stream()
                .filter(e -> e.getKey().equals(COLLIDE_TYPE))
                .findAny();
        Optional<Map.Entry<String, String>> opLimit = original
                .entrySet()
                .stream()
                .filter(e -> e.getKey().equals(BLOCK_LIMIT))
                .findAny();
        if (opBlockType.isEmpty()) {
            return Optional.empty();
        }
        String blockType = opBlockType.get().getValue();

        //something wrong with OPBlockType, value of null
        if (blockType == null) {
            return Optional.empty();
        }

        Optional<BlockType> opType = Parser.STRING_TO_BLOCK_TYPE.parse(blockType);
        if (opType.isEmpty()) {
            return Optional.empty();
        }
        ModifiableBlockInstruction bi = new ModifiableBlockInstruction(opType.get());
        opCollideType
                .flatMap(stringStringEntry -> ShipsParsers.STRING_TO_COLLIDE_TYPE.parse(stringStringEntry.getValue()))
                .ifPresent(bi::setCollide);
        opLimit
                .flatMap(stringLimit -> Parser.STRING_TO_INTEGER.parse(stringLimit.getValue()))
                .ifPresent(bi::setBlockLimit);
        return Optional.of(bi);
    }

    @Override
    public Map<String, String> unparse(@NotNull ModifiableBlockInstruction value) {
        Map<String, String> map = new HashMap<>();
        map.put(BLOCK_TYPE, Parser.STRING_TO_BLOCK_TYPE.unparse(value.getType()));
        map.put(COLLIDE_TYPE, ShipsParsers.STRING_TO_COLLIDE_TYPE.unparse(value.getCollide()));
        map.put(BLOCK_LIMIT, Parser.STRING_TO_INTEGER.unparse(value.getBlockLimit().orElse(-1)));
        return map;
    }

    @Override
    public List<String> getKeys() {
        return Arrays.asList(BLOCK_TYPE, COLLIDE_TYPE, BLOCK_LIMIT);
    }
}





















