package org.ships.config.parsers.identify;

import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.types.ShipType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StringToShipTypeParser extends StringToIdentifiable<ShipType<?>> {

    public StringToShipTypeParser() {
        super((Class<ShipType<?>>) (Object) ShipType.class);
    }

    public static class StringToHostCloneableShipTypeParser extends StringToIdentifiable<CloneableShipType<?>> {

        public StringToHostCloneableShipTypeParser() {
            super((Class<CloneableShipType<?>>) (Object) CloneableShipType.class);
        }

        @Override
        public Optional<CloneableShipType<?>> parse(String original) {
            Collection<CloneableShipType<?>> shipTypes = ShipsPlugin.getPlugin().getAllCloneableShipTypes();
            return shipTypes
                    .stream()
                    .filter(s -> s.getOriginType().equals(s))
                    .filter(s -> s.getId().equals(original))
                    .findAny();
        }

        @Override
        public List<CloneableShipType<?>> getSuggestions() {
            Collection<CloneableShipType<?>> shipTypes = ShipsPlugin.getPlugin().getAllCloneableShipTypes();
            return shipTypes.stream().filter(s -> s.getOriginType().equals(s)).collect(Collectors.toList());
        }
    }
}
