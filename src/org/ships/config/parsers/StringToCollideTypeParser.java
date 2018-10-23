package org.ships.config.parsers;

import org.core.configuration.parser.StringParser;
import org.ships.config.blocks.BlockInstruction;

import java.util.Optional;

public class StringToCollideTypeParser implements StringParser<BlockInstruction.CollideType> {

    @Override
    public Optional<BlockInstruction.CollideType> parse(String original) {
        for (BlockInstruction.CollideType collideType : BlockInstruction.CollideType.values()){
            if(collideType.name().equalsIgnoreCase(original) || collideType.name().charAt(0) == original.charAt(0)){
                return Optional.of(collideType);
            }
        }
        return Optional.empty();
    }

    @Override
    public String unparse(BlockInstruction.CollideType value) {
        return value.name();
    }
}
