package org.ships.config.messages.adapter.entity;

import net.kyori.adventure.text.Component;
import org.core.TranslateCore;
import org.core.entity.Entity;
import org.core.entity.living.human.AbstractHuman;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.Player;
import org.core.utils.Else;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityNameAdapter implements MessageAdapter<Entity<?>> {
    @Override
    public String adapterText() {
        return "Entity Name";
    }

    @Override
    public Class<?> adaptingType() {
        return Entity.class;
    }

    @Override
    public Set<String> examples() {
        Collection<LivePlayer> collection = TranslateCore.getServer().getOnlinePlayers();
        if (collection.isEmpty()) {
            return Collections.singleton("Creeper");
        }
        return collection.stream().map(AbstractHuman::getName).collect(Collectors.toSet());
    }

    @Override
    public Collection<AdapterCategory<Entity<?>>> categories() {
        return List.of(AdapterCategories.ENTITY);
    }

    @Override
    public Component processMessage(@NotNull Entity<?> obj) {
        Component t = obj.getCustomNameComponent().orElse(Component.text(obj.getType().getName()));
        return Else.canCast(obj, Player.class, p -> Component.text(p.getName()), e -> t);
    }
}
