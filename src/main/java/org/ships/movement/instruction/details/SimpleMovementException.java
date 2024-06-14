package org.ships.movement.instruction.details;

import org.core.entity.LiveEntity;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.Messageable;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class SimpleMovementException implements BiConsumer<MovementContext, Throwable> {

    private final Collection<Messageable> messageReceivers = new HashSet<>();

    public SimpleMovementException(Messageable... viewers) {
        this(List.of(viewers));
    }

    public SimpleMovementException(Collection<Messageable> messageReceivers) {
        this.messageReceivers.addAll(messageReceivers);
    }

    @Override
    public void accept(MovementContext context, Throwable throwable) {
        Collection<LiveEntity> entities = context
                .getEntities()
                .keySet()
                .stream()
                .filter(snapshot -> snapshot.getCreatedFrom().isPresent())
                .map(snapshot -> snapshot.getCreatedFrom().get())
                .collect(Collectors.toSet());
        entities.forEach(entity -> entity.setGravity(true));
        context.getAdventureBossBar().ifPresent(bossBar -> {
            entities
                    .stream()
                    .filter(entity -> entity instanceof LivePlayer)
                    .map(entity -> (LivePlayer) entity)
                    .forEach(player -> player.hideBossBar(bossBar));
        });
        if (!(throwable instanceof MoveException)) {
            throwable.printStackTrace();
            return;
        }
        MoveException e = (MoveException) throwable;
        this.messageReceivers.forEach(viewer -> viewer.sendMessage(e.getErrorMessage()));
    }
}
