package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.misc.CollectionAdapter;
import org.ships.config.messages.messages.error.data.RequirementPercentMessageData;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.requirement.SpecialBlocksRequirement;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class ErrorSpecialBlockPercentNotEnough implements Message<RequirementPercentMessageData> {

    @Override
    public String[] getPath() {
        return new String[]{"Error", "SpecialBlocks", "NotEnough"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("Found " + Message.PERCENT_FOUND.adapterTextFormat() + " of one of ");
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        Collection<MessageAdapter<?>> collection = new HashSet<>();
        collection.addAll(Message.VESSEL_ADAPTERS);
        collection.addAll(
                Message.BLOCK_TYPE_ADAPTERS.parallelStream().map(CollectionAdapter::new).collect(Collectors.toSet()));
        collection.add(Message.PERCENT_FOUND);
        collection.add(Message.TOTAL_FOUND_BLOCKS);
        return collection;
    }

    @Override
    public AText process(@NotNull AText text, RequirementPercentMessageData obj) {
        for (MessageAdapter<Vessel> adapter : Message.VESSEL_ADAPTERS) {
            if (adapter.containsAdapter(text)) {
                text = adapter.process(obj.getVessel(), text);
            }
        }
        if (Message.PERCENT_FOUND.containsAdapter(text)) {
            text = Message.PERCENT_FOUND.process(obj.getPercentageMet(), text);
        }
        if (Message.TOTAL_FOUND_BLOCKS.containsAdapter(text)) {
            text = Message.TOTAL_FOUND_BLOCKS.process(obj.getBlocksMeetingRequirements(), text);
        }
        if (!(obj.getVessel() instanceof VesselRequirement vesselRequirement)) {
            return text;
        }
        Optional<SpecialBlocksRequirement> opRequirement = vesselRequirement.getRequirement(
                SpecialBlocksRequirement.class);
        if (opRequirement.isEmpty()) {
            return text;
        }
        Collection<BlockType> blocks = opRequirement.get().getBlocks();
        for (MessageAdapter<BlockType> adapter : Message.BLOCK_TYPE_ADAPTERS) {
            MessageAdapter<Collection<BlockType>> collectionAdapter = new CollectionAdapter<>(adapter);
            if (collectionAdapter.containsAdapter(text)) {
                text = collectionAdapter.process(blocks, text);
            }
        }
        return text;
    }
}
