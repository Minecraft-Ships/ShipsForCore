package org.ships.config.parsers;

import org.core.config.parser.StringParser;
import org.ships.config.blocks.BlockInstruction;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StringToCollideTypeParser implements StringParser.Suggestible<BlockInstruction.CollideType> {

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

    @Override
    public List<BlockInstruction.CollideType> getSuggestions(String peek) {
        return this.getSuggestions().stream().filter(ct -> ct.name().toLowerCase().startsWith(peek.toLowerCase())).collect(Collectors.toList());
    }

    @Override
    public List<BlockInstruction.CollideType> getSuggestions() {
        return Arrays.asList(BlockInstruction.CollideType.values());
    }
}
