package org.ships.config.messages.adapter.entity.type;

import net.kyori.adventure.text.Component;
import org.core.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class EntityTypeNameAdapter implements MessageAdapter<EntityType<?, ?>> {
    @Override
    public String adapterText() {
        return "Entity Type Name";
    }

    @Override
    public Class<?> adaptingType() {
        return EntityType.class;
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("Creeper");
    }

    @Override
    public Collection<AdapterCategory<EntityType<?, ?>>> categories() {
        return List.of(AdapterCategories.ENTITY_TYPE);
    }

    @Override
    public Component processMessage(@NotNull EntityType<?, ?> obj) {
        return Component.text(obj.getName());
    }
}
