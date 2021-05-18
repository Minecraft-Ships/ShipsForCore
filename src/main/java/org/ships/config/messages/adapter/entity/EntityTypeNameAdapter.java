package org.ships.config.messages.adapter.entity;

import org.core.adventureText.AText;
import org.core.entity.Entity;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Collections;
import java.util.Set;

public class EntityTypeNameAdapter implements MessageAdapter<Entity<?>> {
    @Override
    public String adapterText() {
        return "Entity Type Name";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("Creeper");
    }

    @Override
    public AText process(AText message, Entity<?> obj) {
        return message.withAllAs(this.adapterTextFormat(), AText.ofPlain(obj.getType().getName()));
    }
}
