package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.config.ConfigAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ErrorTooManyOfBlockMessage implements Message<Map.Entry<Vessel, BlockType>> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "TooManyOfBlocks"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component
                .text("Too many of " + Message.BLOCK_TYPE_NAME.adapterTextFormat() + " found")
                .color(NamedTextColor.RED);
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        Set<MessageAdapter<?>> set = new HashSet<>();
        set.addAll(Message.VESSEL_ADAPTERS);
        set.addAll(Message.BLOCK_TYPE_ADAPTERS);
        set.addAll(Message.CONFIG_ADAPTERS);
        return set;
    }

    @Override
    public Component processMessage(@NotNull Component text, Map.Entry<Vessel, BlockType> obj) {
        for (ConfigAdapter<?> adapter : Message.CONFIG_ADAPTERS) {
            text = adapter.processMessage(text);
        }
        for (MessageAdapter<Vessel> adapter : Message.VESSEL_ADAPTERS) {
            text = adapter.processMessage(obj.getKey(), text);
        }
        for (MessageAdapter<BlockType> adapter : Message.BLOCK_TYPE_ADAPTERS) {
            text = adapter.processMessage(obj.getValue(), text);
        }
        return text;
    }
}
