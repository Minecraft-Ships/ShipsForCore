package org.ships.movement.instruction.details;

import net.kyori.adventure.bossbar.BossBar;
import org.core.TranslateCore;
import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.movement.MovementContext;
import org.ships.movement.instruction.actions.MidMovement;
import org.ships.movement.instruction.actions.PostMovement;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public class MovementDetails {

    private final @Nullable BlockPosition clickedBlock;
    private final @Nullable BossBar bossBar;
    private final MidMovement[] midMovementEvents;
    private final PostMovement[] postMovementEvents;
    private final Boolean updateStucture;
    private final @NotNull BiConsumer<MovementContext, ? super Throwable> exception;


    public MovementDetails(MovementDetailsBuilder builder) {
        this.clickedBlock = builder.getClickedBlock();
        this.bossBar = builder.getAdventureBossBar();
        this.midMovementEvents = builder.getMidMovementEvents();
        this.postMovementEvents = builder.getPostMovementEvents();
        this.exception = builder.getException();
        this.updateStucture = builder.updatingStructure().orElse(null);
        if (this.exception == null) {
            throw new RuntimeException("Exception must be stated");
        }
    }

    public boolean isUpdatingStructure() {
        return Objects.requireNonNullElse(this.updateStucture, true);
    }

    public @NotNull BiConsumer<MovementContext, ? super Throwable> getException() {
        return this.exception;
    }

    public Optional<BlockPosition> getClickedBlock() {
        return Optional.ofNullable(this.clickedBlock);
    }

    public Optional<BossBar> getAdventureBossBar() {
        return Optional.ofNullable(this.bossBar);
    }

    public @NotNull MidMovement[] getMidMovementEvents() {
        return Objects.requireNonNullElseGet(this.midMovementEvents, () -> new MidMovement[0]);
    }

    public @NotNull PostMovement[] getPostMovementEvents() {
        return Objects.requireNonNullElseGet(this.postMovementEvents, () -> new PostMovement[0]);
    }

    public MovementDetailsBuilder toBuilder() {
        return new MovementDetailsBuilder()
                .setClickedBlock(this.clickedBlock)
                .setAdventureBossBar(this.bossBar)
                .setMidMovementEvents(this.midMovementEvents)
                .setPostMovementEvents(this.postMovementEvents)
                .setException(this.exception);
    }
}
