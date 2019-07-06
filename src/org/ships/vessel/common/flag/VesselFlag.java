package org.ships.vessel.common.flag;

import org.core.configuration.parser.Parser;
import org.core.utils.Identifable;

import java.util.Optional;

public interface VesselFlag<T> extends Identifable {

    interface Serializable<T> extends VesselFlag<T> {
        String serialize();
        Serializable<T> deserialize(String idWithValue);
        boolean isDeserializable(String idWithValue);
    }

    Optional<T> getValue();
    void setValue(T value);
    Parser<? extends Object, T> getParser();
}
