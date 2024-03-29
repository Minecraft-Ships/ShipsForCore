package org.ships.config.messages.adapter.vessel;

import net.kyori.adventure.text.Component;
import org.core.utils.Else;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.exceptions.NoLicencePresent;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.IdentifiableShip;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VesselIdAdapter implements MessageAdapter<Vessel> {
    @Override
    public String adapterText() {
        return "Vessel Id";
    }

    @Override
    public Class<?> adaptingType() {
        return Vessel.class;
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
    public Collection<AdapterCategory<Vessel>> categories() {
        return List.of(AdapterCategories.VESSEL);
    }

    @Override
    public Component processMessage(@NotNull Vessel obj) {
        String name = Else.canCast(obj, IdentifiableShip.class,
                                   identifiableShip -> Else.throwOr(NoLicencePresent.class, identifiableShip::getId,
                                                                    "Unknown"),
                                   vessel -> Else.throwOr(NoLicencePresent.class, vessel::getName, "Unknown"));
        return Component.text(name);
    }

}
