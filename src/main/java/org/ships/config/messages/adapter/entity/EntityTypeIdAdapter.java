package org.ships.config.messages.adapter.entity;

import org.core.adventureText.AText;
import org.core.entity.Entity;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Collections;
import java.util.Set;

public class EntityTypeIdAdapter implements MessageAdapter<Entity<?>> {
    @Override
    public String adapterText() {
        return "Entity Type Id";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("minecraft:creeper");
    }

    @Override
    public AText process(AText message, Entity<?> obj) {
        return message.withAllAs(this.adapterTextFormat(), AText.ofPlain(obj.getType().getId()));
    }
}
