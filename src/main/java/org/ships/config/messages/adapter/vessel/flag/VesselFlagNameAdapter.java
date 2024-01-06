package org.ships.config.messages.adapter.vessel.flag;

import net.kyori.adventure.text.Component;
import org.core.utils.Else;
import org.core.utils.Identifiable;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.vessel.common.flag.VesselFlag;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class VesselFlagNameAdapter implements MessageAdapter<VesselFlag<?>> {
    @Override
    public String adapterText() {
        return "Vessel Key Name";
    }

    @Override
    public Class<?> adaptingType() {
        return VesselFlag.class;
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("Is Moving");
    }

    @Override
    public Collection<AdapterCategory<VesselFlag<?>>> categories() {
        return List.of(AdapterCategories.VESSEL_FLAG);
    }

    @Override
    public Component processMessage(@NotNull VesselFlag<?> obj) {
        return Component.text(Else.canCast(obj, VesselFlag.Serializable.class, (v) -> v.getName() + ":" + v.serialize(),
                                           Identifiable::getName));
    }
}
