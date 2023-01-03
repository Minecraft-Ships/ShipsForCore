package org.ships.vessel.common.requirement;

import org.core.config.ConfigurationStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.util.AbstractMap;
import java.util.Optional;

public class MinSizeRequirement implements Requirement<MinSizeRequirement> {

    private final @Nullable MinSizeRequirement parent;
    private final @Nullable Integer minSize;

    public MinSizeRequirement(@Nullable MinSizeRequirement parent, @Nullable Integer minSize) {
        if (parent == null && minSize == null) {
            throw new IllegalArgumentException("Both parent and Min size cannot be null");
        }
        this.minSize = minSize;
        this.parent = parent;
    }

    public int getMinimumSize() {
        if (this.minSize != null) {
            return this.minSize;
        }
        if (this.parent == null) {
            throw new RuntimeException(
                    "You managed to get passed the constructor check. What you are doing is not supported");
        }
        return this.parent.getMinimumSize();
    }

    @Override
    public boolean useOnStrict() {
        return false;
    }

    @Override
    public void onCheckRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {
        int shipSize = context.getMovingStructure().size() + 1;
        int minSize = this.getMinimumSize();

        if (minSize > shipSize) {
            throw new MoveException(context, AdventureMessageConfig.ERROR_UNDERSIZED,
                                    new AbstractMap.SimpleImmutableEntry<>(vessel, minSize - shipSize));
        }
    }

    @Override
    public void onProcessRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {

    }

    @NotNull
    @Override
    public MinSizeRequirement getRequirementsBetween(@NotNull MinSizeRequirement requirement) {
        MinSizeRequirement minSizeRequirement = this;
        Integer amount = null;
        while (minSizeRequirement != null && minSizeRequirement != requirement) {
            if (minSizeRequirement.minSize != null) {
                amount = minSizeRequirement.minSize;
            }
            minSizeRequirement = minSizeRequirement.getParent().orElse(null);
        }
        return new MinSizeRequirement(requirement, amount);
    }

    @Override
    public @NotNull MinSizeRequirement createChild() {
        return new MinSizeRequirement(this, null);
    }

    public @NotNull MinSizeRequirement createChild(Integer size) {
        return new MinSizeRequirement(this, size);
    }

    @Override
    public @NotNull MinSizeRequirement createCopy() {
        return new MinSizeRequirement(this.parent, this.minSize);
    }

    public @NotNull MinSizeRequirement createCopy(Integer size) {
        return new MinSizeRequirement(this.parent, size);
    }

    @Override
    public Optional<MinSizeRequirement> getParent() {
        return Optional.empty();
    }

    @Override
    public boolean isEnabled() {
        int size = this.getMinimumSize();
        return size > 0;
    }

    @Override
    public void serialize(@NotNull ConfigurationStream stream, boolean withParentData) {
        if (withParentData) {
            int min = this.getMinimumSize();
            stream.set(AbstractShipType.MAX_SIZE, min);
            return;
        }
        if (this.minSize != null) {
            stream.set(AbstractShipType.MAX_SIZE, this.minSize);
        }
    }

    public boolean isMinSizeSpecified() {
        return this.minSize != null;
    }
}
