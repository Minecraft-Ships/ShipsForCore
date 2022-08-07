package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.world.position.block.BlockType;
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
    public AText getDefault() {
        return AText
                .ofPlain("Too many of " + Message.BLOCK_TYPE_NAME.adapterTextFormat() + " found")
                .withColour(NamedTextColours.RED);
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
    public AText process(AText text, Map.Entry<Vessel, BlockType> obj) {
        for (ConfigAdapter adapter : Message.CONFIG_ADAPTERS) {
            text = adapter.process(text);
        }
        for (MessageAdapter<Vessel> adapter : Message.VESSEL_ADAPTERS) {
            text = adapter.process(text, obj.getKey());
        }
        for (MessageAdapter<BlockType> adapter : Message.BLOCK_TYPE_ADAPTERS) {
            text = adapter.process(text, obj.getValue());
        }
        return text;
    }
}
