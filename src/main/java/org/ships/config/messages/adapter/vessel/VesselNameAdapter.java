package org.ships.config.messages.adapter.vessel;

import org.core.adventureText.AText;
import org.core.utils.Else;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.exceptions.NoLicencePresent;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class VesselNameAdapter implements MessageAdapter<Vessel> {
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

    @Override
    public AText process(AText message, Vessel obj) {
        return message.withAllAs(this.adapterTextFormat(), AText.ofPlain(Else.throwOr(NoLicencePresent.class, obj::getName, "Unknown")));
    }
}
