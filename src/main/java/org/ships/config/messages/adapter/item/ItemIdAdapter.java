package org.ships.config.messages.adapter.item;

import net.kyori.adventure.text.Component;
import org.core.inventory.item.ItemType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Set;

public class ItemIdAdapter implements MessageAdapter<ItemType> {
    @Override
    public String adapterText() {
        return "Item Id";
    }

    @Override
    public Set<String> examples() {
        return Set.of("Use " + this.adapterTextFormat() + " to break block");
    }

    @Override
    public Component processMessage(@NotNull ItemType obj) {
        return Component.text(obj.getId());
    }
}
