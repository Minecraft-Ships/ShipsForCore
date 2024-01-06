package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.config.messages.messages.error.data.RequirementPercentMessageData;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.requirement.SpecialBlocksRequirement;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class ErrorSpecialBlockPercentNotEnough implements Message<RequirementPercentMessageData> {


    @Override
    public String[] getPath() {
        return new String[]{"Error", "Requirement", "Blocks", "Not Enough"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("You have " + Message.PERCENT_FOUND.adapterTextFormat() + "% of one of "
                                      + Message.BLOCK_NAMES.adapterTextFormat() + ". You require more");
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL, AdapterCategories.BLOCK_GROUP);
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        Collection<MessageAdapter<?>> collection = new HashSet<>(Message.super.getAdapters());
        collection.add(Message.PERCENT_FOUND);
        collection.add(Message.TOTAL_FOUND_BLOCKS);
        return collection;
    }

    @Override
    public Component processMessage(@NotNull Component text, RequirementPercentMessageData obj) {
        List<MessageAdapter<Vessel>> vesselAdapters = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL).toList();
        List<MessageAdapter<Collection<BlockType>>> blockGroupsAdapters = MessageAdapters
                .getAdaptersFor(AdapterCategories.BLOCK_GROUP)
                .toList();

        for (MessageAdapter<Vessel> adapter : vesselAdapters) {
            text = adapter.processMessage(obj.getVessel(), text);
        }

        if (obj.getVessel() instanceof VesselRequirement shipVessel) {
            Optional<SpecialBlocksRequirement> opRequirement = shipVessel.getRequirement(
                    SpecialBlocksRequirement.class);
            if (opRequirement.isPresent()) {
                for (MessageAdapter<Collection<BlockType>> adapter : blockGroupsAdapters) {
                    text = adapter.processMessage(opRequirement.get().getSpecifiedBlocks(), text);
                }
            }
        }

        text = Message.PERCENT_FOUND.processMessage(obj.getPercentageMet(), text);
        text = Message.TOTAL_FOUND_BLOCKS.processMessage(obj.getBlocksMeetingRequirements(), text);

        return text;
    }
}
