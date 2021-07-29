package org.ships.config.messages.adapter.entity.type;

import org.core.adventureText.AText;
import org.core.entity.EntityType;
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
    public AText process(AText message, EntityType<?, ?> obj) {
        return message.withAllAs(this.adapterTextFormat(), AText.ofPlain(obj.getName()));
    }
}
