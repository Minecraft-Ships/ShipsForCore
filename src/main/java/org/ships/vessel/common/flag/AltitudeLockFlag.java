package org.ships.vessel.common.flag;

import org.core.config.parser.Parser;
import org.core.config.parser.StringParser;

import java.util.Optional;

public class AltitudeLockFlag implements VesselFlag<Boolean> {

    protected boolean value;

    public AltitudeLockFlag() {
        this(false);
    }

    public AltitudeLockFlag(boolean value) {
        this.value = value;
    }

    @Override
    public Optional<Boolean> getValue() {
        return Optional.of(this.value);
    }

    @Override
    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public StringParser<Boolean> getParser() {
        return Parser.STRING_TO_BOOLEAN;
    }

    @Override
    public Builder toBuilder() {
        return new Builder();
    }

    @Override
    public String getId() {
        return "ships:altitude_lock";
    }

    @Override
    public String getName() {
        return "Altitude Lock";
    }

    @Override
    public VesselFlag<Boolean> clone() {
        return VesselFlag.super.clone();
    }

    public static class Builder extends VesselFlag.Builder<Boolean, AltitudeLockFlag> {

        @Override
        protected AltitudeLockFlag buildEmpty() {
            return new AltitudeLockFlag();
        }
    }
}
