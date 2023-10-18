package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.world.position.impl.Position;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.misc.CollectionSingleAdapter;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ErrorNoShipSignMessage implements Message<PositionableShipsStructure> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Move", "NoShipsSign"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component
                .text("Cannot find ")
                .color(NamedTextColor.RED)
                .append(Component.text("[Ships]").color(NamedTextColor.YELLOW))
                .append(Component.text(" sign").color(NamedTextColor.RED));
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(this.getExactAdapters());
    }

    private Set<CollectionSingleAdapter<Position<?>>> getExactAdapters() {
        return Message.LOCATION_ADAPTERS.parallelStream().map(Message::asCollectionSingle).collect(Collectors.toSet());
    }

    @Override
    public Component processMessage(@NotNull Component text, PositionableShipsStructure obj) {
        Collection<Position<?>> positions = obj
                .getAsyncedPositionsRelativeToWorld()
                .parallelStream()
                .collect(Collectors.toSet());
        for (CollectionSingleAdapter<Position<?>> adapter : this.getExactAdapters()) {
            text = adapter.processMessage(positions, text);
        }
        return text;
    }
}
