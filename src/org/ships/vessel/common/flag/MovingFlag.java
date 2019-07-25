package org.ships.vessel.common.flag;

import org.core.configuration.parser.Parser;
import org.ships.movement.MovingBlockSet;

import java.util.Optional;

public class MovingFlag implements VesselFlag<MovingBlockSet> {

    protected MovingBlockSet set;

    public MovingFlag(){
        this(null);
    }

    public MovingFlag(MovingBlockSet set){
        this.set = set;
    }


    @Override
    public Optional<MovingBlockSet> getValue() {
        return Optional.ofNullable(this.set);
    }

    @Override
    public void setValue(MovingBlockSet value) {
        this.set = value;
    }

    @Override
    public Parser<?, MovingBlockSet> getParser() {
        return new Parser<Object, MovingBlockSet>() {
            @Override
            public Optional<MovingBlockSet> parse(Object original) {
                return Optional.empty();
            }

            @Override
            public Object unparse(MovingBlockSet value) {
                return value.size();
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
