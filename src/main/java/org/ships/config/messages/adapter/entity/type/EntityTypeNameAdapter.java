package org.ships.config.messages.adapter.entity.type;

import net.kyori.adventure.text.Component;
import org.core.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Collections;
import java.util.Set;

public class EntityTypeNameAdapter implements MessageAdapter<EntityType<?, ?>> {
    @Override
    public String adapterText() {
        return "Entity Type Name";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("Creeper");
    }

    @Override
    public Component processMessage(@NotNull EntityType<?, ?> obj) {
        return Component.text(obj.getName());
    }
}
