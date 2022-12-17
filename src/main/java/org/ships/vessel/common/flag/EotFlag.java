package org.ships.vessel.common.flag;

import org.core.config.parser.StringParser;
import org.core.vector.type.Vector3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class EotFlag implements VesselFlag<Vector3<Integer>> {

    private @Nullable Vector3<Integer> relative;
    private @Nullable UUID whoChanged;

    public EotFlag(@Nullable UUID whoChanged, @Nullable Vector3<Integer> relative) {
        this.relative = relative;
        this.whoChanged = whoChanged;
    }

    public Optional<UUID> getWhoClicked() {
        return Optional.ofNullable(this.whoChanged);
    }

    public void setWhoClicked(@Nullable UUID uuid) {
        this.whoChanged = uuid;
    }

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
        public EotFlag buildEmpty() {
            return new EotFlag(null, null);
        }
    }
}
