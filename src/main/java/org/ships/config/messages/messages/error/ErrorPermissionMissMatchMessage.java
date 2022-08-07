package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.entity.Entity;
import org.core.entity.living.human.player.LivePlayer;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.config.ConfigAdapter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ErrorPermissionMissMatchMessage implements Message<Map.Entry<LivePlayer, String>> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Permission", "MissMatch"};
    }

    @Override
    public AText getDefault() {
        return AText
                .ofPlain("Missing permission of " + Message.PERMISSION_NODE.adapterTextFormat())
                .withColour(NamedTextColours.RED);
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        Set<MessageAdapter<?>> set = new HashSet<>();
        set.add(Message.PERMISSION_NODE);
        set.addAll(Message.CONFIG_ADAPTERS);
        set.addAll(Message.ENTITY_ADAPTERS);
        return set;
    }

    @Override
    public AText process(AText text, Map.Entry<LivePlayer, String> obj) {
        text = text.withAllAs(Message.PERMISSION_NODE.adapterTextFormat(), AText.ofPlain(obj.getValue()));
        for (ConfigAdapter adapter : Message.CONFIG_ADAPTERS) {
            text = adapter.process(text);
        }
        for (MessageAdapter<Entity<?>> adapter : Message.ENTITY_ADAPTERS) {
            text = adapter.process(text, obj.getKey());
        }
        return text;
    }
}
