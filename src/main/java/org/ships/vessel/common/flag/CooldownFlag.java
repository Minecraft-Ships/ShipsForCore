package org.ships.vessel.common.flag;

import org.core.config.parser.Parser;
import org.core.config.parser.StringParser;
import org.core.utils.time.TimeRange;

import java.util.Optional;

public class CooldownFlag implements VesselFlag<TimeRange> {

    private TimeRange range;

    public CooldownFlag() {
        this(null);
    }

    public CooldownFlag(TimeRange range) {
        this.range = range;
    }

    @Override
    public String getId() {
        return "ships:cooldown";
    }

    @Override
    public String getName() {
        return "Cooldown";
    }

    @Override
    public Optional<TimeRange> getValue() {
        return Optional.ofNullable(this.range);
    }

    @Override
    public void setValue(TimeRange value) {
        this.range = value;
    }

    @Override
    public StringParser<TimeRange> getParser() {
        return Parser.STRING_TO_TIME_RANGE;
    }

    @Override
    public Builder toBuilder() {
        return new Builder();
    }

    public static class Builder extends VesselFlag.Builder<TimeRange, CooldownFlag> {

        @Override
        public CooldownFlag buildEmpty() {
            return new CooldownFlag();
        }
    }
}
