package org.ships.vessel.common.flag;

import org.core.config.parser.StringParser;
import org.core.vector.type.Vector3;

import java.util.Optional;

public class EotFlag implements VesselFlag<Vector3<Integer>> {

    private Vector3<Integer> relative;

    @Override
    public Optional<Vector3<Integer>> getValue() {
        return Optional.ofNullable(this.relative);
    }

    @Override
    public void setValue(Vector3<Integer> value) {
        this.relative = value;
    }

    @Override
    public StringParser<Vector3<Integer>> getParser() {
        return StringParser.STRING_TO_VECTOR3INT;
    }

    @Override
    public Builder toBuilder() {
        return new Builder();
    }

    @Override
    public String getId() {
        return "ships:eot";
    }

    @Override
    public String getName() {
        return "E.O.T";
    }

    public static class Builder extends VesselFlag.Builder<Vector3<Integer>, EotFlag> {

        @Override
        protected EotFlag buildEmpty() {
            return new EotFlag();
        }
    }
}
