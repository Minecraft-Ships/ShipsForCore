package org.ships.config.parsers.identify;

import org.core.configuration.parser.StringParser;
import org.core.utils.Identifable;
import org.ships.plugin.ShipsPlugin;

import java.util.Optional;

public class StringToIdentifiable<T extends Identifable> implements StringParser<T> {

    protected Class<T> class1;

    public StringToIdentifiable(Class<T> class1){
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
}
