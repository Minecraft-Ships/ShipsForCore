package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ErrorNoShipSignMessage implements Message<PositionableShipsStructure> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Sign", "No Ships Sign"};
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
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL_STRUCTURE);
    }

    @Override
    public Component processMessage(@NotNull Component text, PositionableShipsStructure obj) {
        List<MessageAdapter<PositionableShipsStructure>> structureAdapter = MessageAdapters
                .getAdaptersFor(AdapterCategories.VESSEL_STRUCTURE)
                .collect(Collectors.toList());
        for (MessageAdapter<PositionableShipsStructure> adapter : structureAdapter) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }
}
