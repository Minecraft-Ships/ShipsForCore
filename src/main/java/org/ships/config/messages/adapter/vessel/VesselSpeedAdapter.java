package org.ships.config.messages.adapter.vessel;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.vessel.common.types.Vessel;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class VesselSpeedAdapter implements MessageAdapter<Vessel> {
    @Override
    public String adapterText() {
        return "Vessel Speed";
    }

    @Override
    public Class<?> adaptingType() {
        return Vessel.class;
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(new SecureRandom().nextInt(99) + "");
    }

    @Override
    public Collection<AdapterCategory<Vessel>> categories() {
        return List.of(AdapterCategories.VESSEL);
    }

    @Override
    public Component processMessage(@NotNull Vessel obj) {
        return Component.text(obj.getMaxSpeed() + "");
    }
}
