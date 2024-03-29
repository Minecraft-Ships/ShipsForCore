package org.ships.config.parsers.identify;

import org.core.config.parser.StringParser;
import org.core.utils.Identifiable;
import org.ships.plugin.ShipsPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StringToIdentifiable<T extends Identifiable> implements StringParser.Suggestible<T> {

    protected final Class<T> class1;

    public StringToIdentifiable(Class<T> class1) {
        this.class1 = class1;
    }

    @Override
    public Optional<T> parse(String original) {
        return ShipsPlugin.getPlugin().getAll(this.class1).stream().filter(t -> t.getId().equals(original)).findAny();
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
        return new ArrayList<>(ShipsPlugin.getPlugin().getAll(this.class1));
    }
}
