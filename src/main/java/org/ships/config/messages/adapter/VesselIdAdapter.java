package org.ships.config.messages.adapter;

import org.core.utils.Else;
import org.ships.exceptions.NoLicencePresent;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.IdentifiableShip;
import org.ships.vessel.common.types.Vessel;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class VesselIdAdapter implements MessageAdapter {
    @Override
    public String adapterText() {
        return "Vessel Id";
    }

    @Override
    public Set<String> examples() {
        Set<Vessel> vessels = ShipsPlugin.getPlugin().getVessels();
        if (vessels.isEmpty()) {
            return Collections.singleton("ships:watership.sunk");
        }
        return vessels.stream().filter(v -> v instanceof IdentifiableShip).map(v -> Else.throwOr(NoLicencePresent.class, ((IdentifiableShip) v)::getId, "ships:watership.sunk")).collect(Collectors.toSet());
    }
}
