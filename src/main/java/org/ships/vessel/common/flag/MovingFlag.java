package org.ships.vessel.common.flag;

import org.core.config.parser.StringParser;
import org.ships.movement.MovementContext;

import java.util.Optional;

public class MovingFlag implements VesselFlag<MovementContext> {

    public static class Builder extends VesselFlag.Builder<MovementContext, MovingFlag> {

        @Override
        protected MovingFlag buildEmpty() {
            return new MovingFlag();
        }
    }

    protected MovementContext context;

    public MovingFlag(){
        this(null);
    }

    public MovingFlag(MovementContext context){
        this.context = context;
    }


    @Override
    public Optional<MovementContext> getValue() {
        return Optional.ofNullable(this.context);
    }

    @Override
    public void setValue(MovementContext value) {
        this.context = value;
    }

    @Override
    public StringParser<MovementContext> getParser() {
        return new StringParser<MovementContext>() {
            @Override
            public Optional<MovementContext> parse(String original) {
                return Optional.empty();
            }

            @Override
            public String unparse(MovementContext value) {
                return value.toString();
            }
        };
    }

    @Override
    public Builder toBuilder() {
        return new Builder();
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
