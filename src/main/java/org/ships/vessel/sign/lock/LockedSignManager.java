package org.ships.vessel.sign.lock;

import org.core.utils.Bounds;
import org.core.world.WorldExtent;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class LockedSignManager {

    private final Collection<SignLock> lockedSigns = new LinkedBlockingQueue<>();

    public Collection<SignLock> getLockedSigns() {
        Set<SignLock> locked = this.lockedSigns.parallelStream().filter(SignLock::isValid).collect(Collectors.toSet());
        this.lockedSigns.clear();
        this.lockedSigns.addAll(locked);
        return Collections.unmodifiableCollection(locked);
    }

    public Collection<SignLock> getLockedSigns(@NotNull Vessel vessel) {
        Collection<SignLock> locked = this.getLockedSigns();
        WorldExtent world = vessel.getPosition().getWorld();
        Bounds<Integer> bounds = vessel.getStructure().getBounds();
        return locked.parallelStream().filter(lock -> {
            if (lock.getLockedTo().map(lockedTo -> lockedTo.equals(vessel)).orElse(false)) {
                return true;
            }
            if (!lock.getPosition().getWorld().equals(world)) {
                return false;
            }
            return bounds.contains(lock.getPosition().getPosition());
        }).collect(Collectors.toUnmodifiableSet());
    }

    public boolean isLocked(Position<?> position) {
        BlockPosition blockPos = position.toBlockPosition();
        return this.getLockedSigns().parallelStream().anyMatch(lock -> lock.getPosition().equals(blockPos));
    }

    public void lock(@NotNull Position<?> position) {
        this.lock(position, null);
    }

    public void lock(@NotNull Position<?> position, @Nullable Vessel to) {
        this.lockedSigns.add(new SignLock(to, position.toBlockPosition()));
    }

    public void unlock(@NotNull Position<?> position) {
        BlockPosition block = position.toBlockPosition();
        this.lockedSigns
                .parallelStream()
                .filter(p -> p.getPosition().equals(block))
                .findAny()
                .ifPresent(this.lockedSigns::remove);
    }

    public boolean unlockAll(Collection<SignLock> locks) {
        return this.lockedSigns.removeAll(locks);
    }
}
