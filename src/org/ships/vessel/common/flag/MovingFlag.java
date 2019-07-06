package org.ships.vessel.common.flag;

import org.core.configuration.parser.Parser;

import java.util.Optional;

public class MovingFlag implements VesselFlag<Boolean> {

    protected boolean check;

    public MovingFlag(){
        this(false);
    }

    public MovingFlag(boolean check){
        this.check = check;
    }


    @Override
    public Optional<Boolean> getValue() {
        return Optional.of(this.check);
    }

    @Override
    public void setValue(Boolean value) {
        this.check = value;
    }

    @Override
    public Parser<?, Boolean> getParser() {
        return Parser.STRING_TO_BOOLEAN;
    }

    @Override
    public String getId() {
        return "ships.is_moving";
    }

    @Override
    public String getName() {
        return "Is Moving";
    }
}
