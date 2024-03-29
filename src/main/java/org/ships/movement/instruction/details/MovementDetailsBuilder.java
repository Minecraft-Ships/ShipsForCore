package org.ships.movement.instruction.details;

import net.kyori.adventure.bossbar.BossBar;
import org.core.TranslateCore;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.movement.MovementContext;
import org.ships.movement.instruction.actions.MidMovement;
import org.ships.movement.instruction.actions.PostMovement;

import java.util.Optional;
import java.util.function.BiConsumer;

public class MovementDetailsBuilder {

    private BlockPosition clickedBlock;
    private BossBar bossBar;
    private MidMovement[] midMovementEvents;
    private PostMovement[] postMovementEvents;
    private BiConsumer<MovementContext, ? super Throwable> exception;
    private Boolean updatingStructure;

    public Optional<Boolean> updatingStructure() {
        return Optional.ofNullable(this.updatingStructure);
    }

    public MovementDetailsBuilder setUpdatingStructure(Boolean updatingStructure) {
        this.updatingStructure = updatingStructure;
        return this;
    }

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

    @Deprecated(forRemoval = true)
    public ServerBossBar getBossBar() {
        return TranslateCore.createBossBar(getAdventureBossBar());
    }

    @Deprecated(forRemoval = true)
    public MovementDetailsBuilder setBossBar(ServerBossBar bossBar) {
        return setAdventureBossBar(bossBar.bossBar());
    }

    public BossBar getAdventureBossBar() {
        return this.bossBar;
    }

    public MovementDetailsBuilder setAdventureBossBar(BossBar bar) {
        this.bossBar = bar;
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
