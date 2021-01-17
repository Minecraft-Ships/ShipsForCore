package org.ships.vessel.common.flag;

import org.core.config.parser.StringParser;
import org.core.utils.Identifable;

import java.util.Optional;

public interface VesselFlag<T> extends Identifable {

    abstract class Builder<T, F extends VesselFlag<T>> {

        protected abstract F buildEmpty();

        public F build(String parse){
            F flag = buildEmpty();
            Optional<T> opValue = flag.getParser().parse(parse);
            if(!opValue.isPresent()){
                throw new IllegalArgumentException("Could not parse \"" + parse + "\"");
            }
            flag.setValue(opValue.get());
            return flag;
        }

    }

    interface Serializable<T> extends VesselFlag<T> {
        String serialize();
        Serializable<T> deserialize(String idWithValue);
        boolean isDeserializable(String idWithValue);
    }

    Optional<T> getValue();
    void setValue(T value);
    StringParser<T> getParser();
    VesselFlag.Builder<T, ? extends VesselFlag<T>> toBuilder();
}
