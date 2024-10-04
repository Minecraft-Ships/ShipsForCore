package org.ships.config.parsers.identify;

import org.core.config.parser.StringParser;
import org.core.utils.Identifiable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringToIdentifiable<T extends Identifiable> implements StringParser.Suggestible<T> {

    protected final Supplier<Stream<T>> all;

    public StringToIdentifiable(Supplier<Stream<T>> class1) {
        this.all = class1;
    }

    @Override
    public Optional<T> parse(String original) {
        return this.all.get().filter(t -> t.getId().equals(original)).findAny();
    }

    @Override
    public String unparse(T value) {
        return value.getId();
    }

    @Override
    public List<T> getSuggestions(String peek) {
        return this
                .getSuggestions()
                .stream()
                .filter(i -> i.getId().toLowerCase().startsWith(peek.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<T> getSuggestions() {
        return this.all.get().collect(Collectors.toList());
    }
}
