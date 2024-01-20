package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.entity.living.human.player.LivePlayer;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ErrorPermissionMissMatchMessage implements Message<Map.Entry<LivePlayer, String>> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Permission", "Miss Match"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component
                .text("Missing permission of " + Message.PERMISSION_NODE.adapterTextFormat())
                .color(NamedTextColor.RED);
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.PLAYER, AdapterCategories.PERMISSION);
    }

    @Override
    public Component processMessage(@NotNull Component text, Map.Entry<LivePlayer, String> obj) {
        List<MessageAdapter<String>> permissionAdapters = MessageAdapters
                .getAdaptersFor(AdapterCategories.PERMISSION)
                .collect(Collectors.toList());
        List<MessageAdapter<LivePlayer>> playerAdapters = MessageAdapters
                .getAdaptersFor(AdapterCategories.PLAYER)
                .collect(Collectors.toList());

        for (MessageAdapter<String> adapter : permissionAdapters) {
            text = adapter.processMessage(obj.getValue(), text);
        }
        for (MessageAdapter<LivePlayer> adapter : playerAdapters) {
            text = adapter.processMessage(obj.getKey(), text);
        }
        return text;
    }
}
