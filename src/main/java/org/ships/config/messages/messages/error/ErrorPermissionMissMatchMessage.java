package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.entity.Entity;
import org.core.entity.living.human.player.LivePlayer;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.config.ConfigAdapter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ErrorPermissionMissMatchMessage implements Message<Map.Entry<LivePlayer, String>> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Permission", "MissMatch"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component
                .text("Missing permission of " + Message.PERMISSION_NODE.adapterTextFormat())
                .color(NamedTextColor.RED);
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
    public Component processMessage(@NotNull Component text, Map.Entry<LivePlayer, String> obj) {
        text = text.replaceText(TextReplacementConfig
                                        .builder()
                                        .replacement(obj.getValue())
                                        .match(Pattern.compile(Message.PERMISSION_NODE.adapterTextFormat(),
                                                               Pattern.CASE_INSENSITIVE))
                                        .build());
        for (ConfigAdapter<?> adapter : Message.CONFIG_ADAPTERS) {
            text = adapter.processMessage(text);
        }
        for (MessageAdapter<Entity<?>> adapter : Message.ENTITY_ADAPTERS) {
            text = adapter.processMessage(obj.getKey(), text);
        }
        return text;
    }
}
