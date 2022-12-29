package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Collection;
import java.util.stream.Collectors;

public class ErrorFailedToFindLicenceSignMessage implements Message<PositionableShipsStructure> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "FailedToFindLicenceSign"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain("Failed to find licence sign");
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        return Message.STRUCTURE_ADAPTERS.parallelStream().collect(Collectors.toSet());
    }

    @Override
    public AText process(@NotNull AText text, PositionableShipsStructure obj) {
        for (MessageAdapter<?> adapter : this.getAdapters()) {
            if (adapter.containsAdapter(text)) {
                text = ((MessageAdapter<PositionableShipsStructure>) adapter).process(obj, text);
            }
        }
        return text;
    }
}
