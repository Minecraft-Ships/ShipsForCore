package org.ships.config.messages.adapter.structure;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class StructureSizeAdapter implements MessageAdapter<PositionableShipsStructure> {
    @Override
    public String adapterText() {
        return "Structure size";
    }

    @Override
    public Class<?> adaptingType() {
        return PositionableShipsStructure.class;
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(new SecureRandom().nextInt(99) + "");
    }

    @Override
    public Collection<AdapterCategory<PositionableShipsStructure>> categories() {
        return List.of(AdapterCategories.VESSEL_STRUCTURE);
    }

    @Override
    public Component processMessage(@NotNull PositionableShipsStructure obj) {
        return Component.text((obj.size() + 1) + "");
    }
}
