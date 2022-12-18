package org.ships.vessel.sign.lock;

import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.movement.MovementContext;
import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;

public class SignLock {

    private final @Nullable Vessel lockedTo;
    private final @NotNull BlockPosition position;

    public SignLock(@Nullable Vessel lockedTo, @NotNull BlockPosition position) {
        this.lockedTo = lockedTo;
        this.position = position;
    }

    public boolean isValid() {
        SyncPosition<Integer> position = this.position.getWorld().getPosition(this.position.getPosition());
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if (opTile.isEmpty()) {
            return false;
        }
        if (!(opTile.get() instanceof LiveSignTileEntity)) {
            return false;
        }
        if (this.lockedTo == null) {
            return false;
        }
        Optional<MovementContext> opMovingFlag = this.lockedTo.getValue(MovingFlag.class);
        return opMovingFlag.isPresent();
    }

    public Optional<Vessel> getLockedTo() {
        return Optional.ofNullable(this.lockedTo);
    }

    public @NotNull BlockPosition getPosition() {
        return this.position;
    }
}
