package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.world.position.impl.Position;
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
    public AText getDefault() {
        return AText
                .ofPlain("Cannot find ")
                .withColour(NamedTextColours.RED)
                .append(AText.ofPlain("[Ships]").withColour(NamedTextColours.YELLOW))
                .append(AText.ofPlain(" sign").withColour(NamedTextColours.RED));
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(this.getExactAdapters());
    }

    private Set<CollectionSingleAdapter<Position<?>>> getExactAdapters() {
        return Message.LOCATION_ADAPTERS.parallelStream().map(Message::asCollectionSingle).collect(Collectors.toSet());
    }

    @Override
    public AText process(AText text, PositionableShipsStructure obj) {
        Collection<Position<?>> positions = obj.getAsyncedPositions().parallelStream().collect(Collectors.toSet());
        for (CollectionSingleAdapter<Position<?>> adapter : this.getExactAdapters()) {
            text = adapter.process(positions, text);
        }
        return text;
    }
}
