package org.ships.vessel.common.flag;

import org.core.configuration.parser.Parser;

import java.util.Optional;

public class SuccessfulMoveFlag implements VesselFlag<Boolean>{

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
    public Parser<?, Boolean> getParser() {
        return null;
    }

    @Override
    public String getId() {
        return "ships:move_success";
    }

    @Override
    public String getName() {
        return "Has successfully moved";
    }
}
