package org.ships.config.messages.adapter.vessel;

import org.core.adventureText.AText;
import org.core.utils.Else;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.exceptions.NoLicencePresent;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.IdentifiableShip;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class VesselIdAdapter implements MessageAdapter<Vessel> {
    @Override
    public String adapterText() {
        return "Vessel Id";
    }

    @Override
    public Set<String> examples() {
        Collection<Vessel> vessels = ShipsPlugin.getPlugin().getVessels();
        if (vessels.isEmpty()) {
            return Collections.singleton("ships:watership.sunk");
        }
        return vessels
                .stream()
                .filter(v -> v instanceof IdentifiableShip)
                .map(v -> Else.throwOr(NoLicencePresent.class, ((IdentifiableShip) v)::getId, "ships:watership.sunk"))
                .collect(Collectors.toSet());
    }

    @Override
    public AText process(AText message, Vessel obj) {
        return message
                .withAllAs(
                        this.adapterTextFormat(),
                        AText.ofPlain(
                                Else.canCast(
                                        obj,
                                        IdentifiableShip.class,
                                        identifiableShip -> Else.throwOr(NoLicencePresent.class,
                                                identifiableShip::getId, "Unknown"),
                                        vessel -> Else.throwOr(NoLicencePresent.class, vessel::getName, "Unknown")
                                )
                        )
                );
    }
}
