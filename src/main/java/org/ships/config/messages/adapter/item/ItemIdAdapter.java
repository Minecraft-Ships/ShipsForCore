package org.ships.config.messages.adapter.item;

import org.core.adventureText.AText;
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
    public AText process(@NotNull ItemType obj) {
        return AText.ofPlain(obj.getId());
    }
}
