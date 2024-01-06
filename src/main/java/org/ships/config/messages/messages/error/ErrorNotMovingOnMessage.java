package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.config.messages.messages.error.data.NotMovingOnMessageData;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.List;

public class ErrorNotMovingOnMessage implements Message<NotMovingOnMessageData> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Requirement", "Not In Moving In"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("Cannot move. Needs to move onto either %Block Names%");
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL, AdapterCategories.BLOCK_GROUP);
    }

    @Override
    public Component processMessage(@NotNull Component text, NotMovingOnMessageData obj) {
        List<MessageAdapter<Vessel>> vesselAdapters = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL).toList();
        List<MessageAdapter<Collection<BlockType>>> blockAdapters = MessageAdapters
                .getAdaptersFor(AdapterCategories.BLOCK_GROUP)
                .toList();

        for (MessageAdapter<Vessel> adapter : vesselAdapters) {
            text = adapter.processMessage(obj.getVessel(), text);
        }
        for (MessageAdapter<Collection<BlockType>> adapter : blockAdapters) {
            text = adapter.processMessage(obj.getMoveInMaterials(), text);
        }
        return text;
    }
}
