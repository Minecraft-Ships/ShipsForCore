package org.ships.config.messages.adapter.item;

import net.kyori.adventure.text.Component;
import org.core.inventory.item.ItemType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ItemIdAdapter implements MessageAdapter<ItemType> {
    @Override
    public String adapterText() {
        return "Item Id";
    }

    @Override
    public Class<?> adaptingType() {
        return ItemType.class;
    }

    @Override
    public Set<String> examples() {
        return Set.of("Use " + this.adapterTextFormat() + " to break block");
    }

    @Override
    public Collection<AdapterCategory<ItemType>> categories() {
        return List.of(AdapterCategories.ITEM_TYPE);
    }

    @Override
    public Component processMessage(@NotNull ItemType obj) {
        return Component.text(obj.getId());
    }
}
