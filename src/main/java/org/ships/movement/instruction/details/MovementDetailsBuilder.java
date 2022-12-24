package org.ships.movement.instruction.details;

import org.core.world.boss.ServerBossBar;
import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.movement.MovementContext;
import org.ships.movement.instruction.actions.MidMovement;
import org.ships.movement.instruction.actions.PostMovement;

import java.util.function.BiConsumer;

public class MovementDetailsBuilder {

    private BlockPosition clickedBlock;
    private ServerBossBar bossBar;
    private MidMovement[] midMovementEvents;
    private PostMovement[] postMovementEvents;
    private BiConsumer<MovementContext, ? super Throwable> exception;

    public BiConsumer<MovementContext, ? super Throwable> getException() {
        return this.exception;
    }

    public MovementDetailsBuilder setException(BiConsumer<MovementContext, ? super Throwable> throwable) {
        this.exception = throwable;
        return this;
    }

    public BlockPosition getClickedBlock() {
        return this.clickedBlock;
    }

    public MovementDetailsBuilder setClickedBlock(BlockPosition clickedBlock) {
        this.clickedBlock = clickedBlock;
        return this;
    }

    public ServerBossBar getBossBar() {
        return this.bossBar;
    }

    public MovementDetailsBuilder setBossBar(ServerBossBar bossBar) {
        this.bossBar = bossBar;
        return this;
    }

    public MidMovement[] getMidMovementEvents() {
        return this.midMovementEvents;
    }

    public MovementDetailsBuilder setMidMovementEvents(MidMovement... midMovementEvents) {
        this.midMovementEvents = midMovementEvents;
        return this;
    }

    public PostMovement[] getPostMovementEvents() {
        return this.postMovementEvents;
    }

    public MovementDetailsBuilder setPostMovementEvents(PostMovement... postMovementEvents) {
        this.postMovementEvents = postMovementEvents;
        return this;
    }

    public @NotNull MovementDetails build() {
        return new MovementDetails(this);
    }
}
