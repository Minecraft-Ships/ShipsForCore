package org.ships.movement.instruction.details;

import org.core.entity.LiveEntity;
import org.core.source.viewer.CommandViewer;
import org.core.world.boss.ServerBossBar;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class SimpleMovementException implements BiConsumer<MovementContext, Throwable> {

    private final Collection<CommandViewer> messageReceivers = new HashSet<>();

    public SimpleMovementException(CommandViewer... viewers) {
        this(List.of(viewers));
    }

    public SimpleMovementException(Collection<CommandViewer> messageReceivers) {
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
        context.getBossBar().ifPresent(ServerBossBar::deregisterPlayers);
        if (!(throwable instanceof MoveException e)) {
            throwable.printStackTrace();
            return;
        }
        this.messageReceivers.forEach(viewer -> viewer.sendMessage(e.getErrorMessageText()));
    }
}
