package org.ships.config.messages.adapter.vessel.info;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.*;

public class VesselInfoValueAdapter implements MessageAdapter<Map.Entry<String, String>> {
    @Override
    public String adapterText() {
        return "Vessel Info Value";
    }

    @Override
    public Class<?> adaptingType() {
        return Map.Entry.class;
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("1");
    }

    @Override
    public Collection<AdapterCategory<Map.Entry<String, String>>> categories() {
        return List.of(AdapterCategories.VESSEL_INFO);
    }

    @Override
    public Component processMessage(@NotNull Map.Entry<String, String> obj) {
        return Component.text(obj.getKey());
    }
}
