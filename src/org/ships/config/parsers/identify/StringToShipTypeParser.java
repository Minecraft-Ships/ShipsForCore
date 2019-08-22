package org.ships.config.parsers.identify;

import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.types.ShipType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StringToShipTypeParser extends StringToIdentifiable<ShipType> {

    public static class StringToHostClonableShipTypeParser extends StringToIdentifiable<CloneableShipType>{

        public StringToHostClonableShipTypeParser() {
            super(CloneableShipType.class);
        }

        @Override
        public Optional<CloneableShipType> parse(String original) {
            Collection<CloneableShipType> shipTypes = ShipsPlugin.getPlugin().getAll(CloneableShipType.class);
            return shipTypes.stream().filter(s -> s.getOriginType().equals(s)).filter(s -> s.getId().equals(original)).findAny();
        }

        @Override
        public List<CloneableShipType> getSuggestions() {
            Collection<CloneableShipType> shipTypes = ShipsPlugin.getPlugin().getAll(CloneableShipType.class);
            return shipTypes.stream().filter(s -> s.getOriginType().equals(s)).collect(Collectors.toList());
        }
    }

    public StringToShipTypeParser() {
        super(ShipType.class);
    }
}
