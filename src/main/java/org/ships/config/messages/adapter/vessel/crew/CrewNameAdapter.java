package org.ships.config.messages.adapter.vessel.crew;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.permissions.vessel.CrewPermission;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CrewNameAdapter implements MessageAdapter<CrewPermission> {
    @Override
    public String adapterText() {
        return "Crew Name";
    }

    @Override
    public Class<?> adaptingType() {
        return CrewPermission.class;
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(CrewPermission.CREW_MEMBER.getName());
    }

    @Override
    public Collection<AdapterCategory<CrewPermission>> categories() {
        return List.of(AdapterCategories.CREW_PERMISSION);
    }

    @Override
    public Component processMessage(@NotNull CrewPermission obj) {
        return Component.text(obj.getName());
    }
}
