package org.ships.config.messages.messages.bar;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.config.ConfigAdapter;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.HashSet;
import java.util.Set;

public class BlockFinderBarMessage implements Message<PositionableShipsStructure> {
    @Override
    public String[] getPath() {
        return new String[]{"Bar", "BlockFinder", "OnFind"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text(
                Message.STRUCTURE_SIZE.adapterTextFormat() + " / " + Message.CONFIG_TRACK_LIMIT.adapterTextFormat());
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        Set<MessageAdapter<?>> set = new HashSet<>();
        set.addAll(Message.STRUCTURE_ADAPTERS);
        set.addAll(Message.CONFIG_ADAPTERS);
        return set;
    }

    @Override
    public Component processMessage(@NotNull Component text, PositionableShipsStructure obj) {
        for (ConfigAdapter<?> adapter : Message.CONFIG_ADAPTERS) {
            text = adapter.processMessage(text);
        }
        for (MessageAdapter<PositionableShipsStructure> adapter : Message.STRUCTURE_ADAPTERS) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }
}
