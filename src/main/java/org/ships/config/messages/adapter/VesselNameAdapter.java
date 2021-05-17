package org.ships.config.messages.adapter;

import org.core.utils.Else;
import org.ships.exceptions.NoLicencePresent;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class VesselNameAdapter implements MessageAdapter {
    @Override
    public String adapterText() {
        return "Vessel Name";
    }

    @Override
    public Set<String> examples() {
        Set<Vessel> vessels = ShipsPlugin.getPlugin().getVessels();
        if (vessels.isEmpty()) {
            return Collections.singleton("Sunk");
        }
        return vessels.stream().map(v -> Else.throwOr(NoLicencePresent.class, v::getName, "Sunk")).collect(Collectors.toSet());
    }
}
