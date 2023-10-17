package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
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
    public Component getDefaultMessage() {
        return Component.text("Failed to find licence sign");
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        return Message.STRUCTURE_ADAPTERS.parallelStream().collect(Collectors.toSet());
    }

    @Override
    public Component processMessage(@NotNull Component text, PositionableShipsStructure obj) {
        for (MessageAdapter<?> adapter : this.getAdapters()) {
            if (adapter.containsAdapter(text)) {
                text = ((MessageAdapter<PositionableShipsStructure>) adapter).processMessage(obj, text);
            }
        }
        return text;
    }
}
