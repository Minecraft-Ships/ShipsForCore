package org.ships.config.parsers;

import org.core.config.parser.StringParser;
import org.ships.config.blocks.instruction.CollideType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StringToCollideTypeParser implements StringParser.Suggestible<CollideType> {

    @Override
    public Optional<CollideType> parse(String original) {
        for (CollideType collideType : CollideType.values()) {
            if (collideType.name().equalsIgnoreCase(original) || collideType.name().charAt(0) == original.charAt(0)) {
                return Optional.of(collideType);
            }
        }
        return Optional.empty();
    }

    @Override
    public String unparse(CollideType value) {
        return value.name();
    }

    @Override
    public List<CollideType> getSuggestions(String peek) {
        return this
                .getSuggestions()
                .stream()
                .filter(ct -> ct.name().toLowerCase().startsWith(peek.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CollideType> getSuggestions() {
        return Arrays.asList(CollideType.values());
    }
}
