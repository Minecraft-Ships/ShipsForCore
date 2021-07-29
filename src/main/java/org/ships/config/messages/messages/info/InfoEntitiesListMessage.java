package org.ships.config.messages.messages.info;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.entity.Entity;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.HashSet;
import java.util.Set;

public class InfoEntitiesListMessage implements Message<Entity<?>> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Entities", "List"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain(Message.ENTITY_NAME.adapterTextFormat()).withColour(NamedTextColours.GOLD);
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(this.getExactAdapters());
    }

    @Override
    public AText process(AText text, Entity<?> obj) {
        for (MessageAdapter<Entity<?>> adapter : getExactAdapters()) {
            text = adapter.process(text, obj);
        }
        return text;

    }

    private Set<MessageAdapter<Entity<?>>> getExactAdapters() {
        return new HashSet<>(Message.ENTITY_ADAPTERS);
    }
}
