package org.ships.vessel.common.flag;

import org.core.configuration.parser.Parser;
import org.ships.movement.MovementContext;

import java.util.Optional;

public class MovingFlag implements VesselFlag<MovementContext> {

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
    public Parser<?, MovementContext> getParser() {
        return new Parser<Object, MovementContext>() {
            @Override
            public Optional<MovementContext> parse(Object original) {
                return Optional.empty();
            }

            @Override
            public Object unparse(MovementContext value) {
                return value;
            }
        };
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
