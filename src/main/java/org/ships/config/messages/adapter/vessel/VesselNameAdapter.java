package org.ships.config.messages.adapter.vessel;

import net.kyori.adventure.text.Component;
import org.core.utils.Else;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.exceptions.NoLicencePresent;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VesselNameAdapter implements MessageAdapter<Vessel> {
    @Override
    public String adapterText() {
        return "Vessel Name";
    }

    @Override
    public Class<?> adaptingType() {
        return Vessel.class;
    }

    @Override
    public Set<String> examples() {
        Collection<Vessel> vessels = ShipsPlugin.getPlugin().getVessels();
        if (vessels.isEmpty()) {
            return Collections.singleton("Sunk");
        }
        return vessels
                .stream()
                .map(v -> Else.throwOr(NoLicencePresent.class, v::getName, "Sunk"))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<AdapterCategory<Vessel>> categories() {
        return List.of(AdapterCategories.VESSEL);
    }

    @Override
    public Component processMessage(@NotNull Vessel obj) {
        return Component.text(Else.throwOr(NoLicencePresent.class, obj::getName, "Unknown"));
    }
}
