package org.ships.vessel.common.flag;

import org.core.config.parser.Parser;
import org.core.config.parser.StringParser;

import java.util.Optional;

public class SuccessfulMoveFlag implements VesselFlag<Boolean> {

    private boolean successful;

    @Override
    public Optional<Boolean> getValue() {
        return Optional.of(this.successful);
    }

    @Override
    public void setValue(Boolean value) {
        this.successful = value;
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
        return "ships:move_success";
    }

    @Override
    public String getName() {
        return "Has successfully moved";
    }

    public static class Builder extends VesselFlag.Builder<Boolean, SuccessfulMoveFlag> {

        @Override
        protected SuccessfulMoveFlag buildEmpty() {
            return new SuccessfulMoveFlag();
        }
    }
}
