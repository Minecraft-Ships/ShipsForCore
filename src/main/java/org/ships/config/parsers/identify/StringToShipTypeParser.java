package org.ships.config.parsers.identify;

import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipTypes;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StringToShipTypeParser extends StringToIdentifiable<ShipType<?>> {

    public StringToShipTypeParser() {
        super(() -> ShipTypes.shipTypes().stream());
    }

    public static class StringToHostCloneableShipTypeParser extends StringToIdentifiable<CloneableShipType<?>> {

        public StringToHostCloneableShipTypeParser() {
            super(ShipTypes::cloneableShipTypes);
        }

        @Override
        public Optional<CloneableShipType<?>> parse(String original) {
            return ShipTypes
                    .cloneableShipTypes()
                    .filter(s -> s.getOriginType().equals(s))
                    .filter(s -> s.getId().equals(original))
                    .findAny();
        }

        @Override
        public List<CloneableShipType<?>> getSuggestions() {
            return ShipTypes.cloneableShipTypes().filter(s -> s.getOriginType().equals(s)).collect(Collectors.toList());
        }
    }
}
