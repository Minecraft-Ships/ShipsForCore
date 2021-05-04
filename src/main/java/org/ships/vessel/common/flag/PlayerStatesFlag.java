package org.ships.vessel.common.flag;

import org.array.utils.ArrayUtils;
import org.core.config.parser.StringParser;
import org.core.vector.type.Vector3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class PlayerStatesFlag implements VesselFlag.Serializable<Map<UUID, Vector3<Double>>> {

    public static class Builder extends VesselFlag.Builder<Map<UUID, Vector3<Double>>, PlayerStatesFlag> {

        @Override
        protected PlayerStatesFlag buildEmpty() {
            return new PlayerStatesFlag();
        }
    }

    private Map<UUID, Vector3<Double>> playerStates = new HashMap<>();

    @Override
    public String getId() {
        return "ships:player_states";
    }

    @Override
    public String getName() {
        return "Player States";
    }

    @Override
    public Optional<Map<UUID, Vector3<Double>>> getValue() {
        return Optional.of(this.playerStates);
    }

    @Override
    public void setValue(Map<UUID, Vector3<Double>> value) {
        this.playerStates = value;
    }

    @Override
    public StringParser<Map<UUID, Vector3<Double>>> getParser() {
        return new StringParser<Map<UUID, Vector3<Double>>>() {

            @Override
            public Optional<Map<UUID, Vector3<Double>>> parse(String original) {
                Map<UUID, Vector3<Double>> map = new HashMap<>();
                String[] split = original.split(", ");
                for (String pair : split) {
                    try {
                        String[] entry = pair.split(": ");
                        UUID uuid = UUID.fromString(entry[0]);
                        String[] vectorPoints = entry[1].split(Pattern.quote("||"));
                        Vector3<Double> vector = Vector3.valueOf(Double.parseDouble(vectorPoints[0]), Double.parseDouble(vectorPoints[1]), Double.parseDouble(vectorPoints[2]));
                        map.put(uuid, vector);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                return Optional.of(map);
            }

            @Override
            public String unparse(Map<UUID, Vector3<Double>> value) {
                return ArrayUtils.toString(", ", e -> e.getKey().toString() + ": " + e.getValue().getX() + "||" + e.getValue().getY() + "||" + e.getValue().getZ(), value.entrySet());
            }
        };
    }

    @Override
    public Builder toBuilder() {
        return new PlayerStatesFlag.Builder();
    }

    @Override
    public String serialize() {
        return this.getParser().unparse(this.playerStates);
    }

    @Override
    public Serializable<Map<UUID, Vector3<Double>>> deserialize(String idWithValue) {
        this.playerStates = this.getParser().parse(idWithValue).get();
        return this;
    }

    @Override
    public boolean isDeserializable(String idWithValue) {
        Optional<Map<UUID, Vector3<Double>>> opMap = this.getParser().parse(idWithValue);
        return opMap.filter(uuidVector3Map -> !uuidVector3Map.isEmpty()).isPresent();
    }
}
