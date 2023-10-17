package org.ships.config.messages.adapter.entity.type;

import net.kyori.adventure.text.Component;
import org.core.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Collections;
import java.util.Set;

public class EntityTypeIdAdapter implements MessageAdapter<EntityType<?, ?>> {
    @Override
    public String adapterText() {
        return "Entity Type Id";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("minecraft:creeper");
    }

    @Override
    public Component processMessage(@NotNull EntityType<?, ?> obj) {
        return Component.text(obj.getId());
    }
}
