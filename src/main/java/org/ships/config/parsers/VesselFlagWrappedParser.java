package org.ships.config.parsers;

import org.core.config.parser.StringParser;
import org.ships.vessel.common.flag.VesselFlag;

import java.util.Optional;

public class VesselFlagWrappedParser<T> implements StringParser<VesselFlag<T>> {

    private final VesselFlag.Builder<T, ? extends VesselFlag<T>> builder;

    public VesselFlagWrappedParser(VesselFlag.Builder<T, ? extends VesselFlag<T>> builder) {
        this.builder = builder;
    }

    @Override
    public Optional<VesselFlag<T>> parse(String original) {
        try {
            return Optional.of(this.builder.build(original));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public String unparse(VesselFlag<T> value) {
        Optional<T> opValue = value.getValue();
        return opValue.map(t -> value.getParser().unparse(t)).orElse(null);
    }
}
