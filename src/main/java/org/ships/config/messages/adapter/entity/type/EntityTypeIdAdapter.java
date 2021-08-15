package org.ships.config.messages.adapter.entity.type;

import org.core.adventureText.AText;
import org.core.entity.EntityType;
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
    public AText process(AText message, EntityType<?, ?> obj) {
        return message.withAllAs(this.adapterTextFormat(), AText.ofPlain(obj.getId()));
    }
}