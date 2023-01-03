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
import java.util.OptionalInt;

public class MaxSizeRequirement implements Requirement<MaxSizeRequirement> {

    private final @Nullable Integer maxSize;
    private final @Nullable MaxSizeRequirement parent;

    public MaxSizeRequirement(@NotNull MaxSizeRequirement parent) {
        this(parent, null);
    }

    public MaxSizeRequirement(@Nullable MaxSizeRequirement parent, @Nullable Integer maxSize) {
        if (maxSize != null && maxSize < 0) {
            throw new IllegalArgumentException("Max size cannot be less then 0");
        }
        this.maxSize = maxSize;
        this.parent = parent;
    }

    public boolean isMaxSizeSpecified() {
        return this.maxSize != null;
    }

    public OptionalInt getMaxSize() {
        if (this.maxSize != null) {
            return OptionalInt.of(this.maxSize);
        }
        if (this.parent == null) {
            return OptionalInt.empty();
        }
        return this.parent.getMaxSize();
    }

    @Override
    public boolean useOnStrict() {
        return false;
    }

    @Override
    public void onCheckRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {
        int size = context.getMovingStructure().size() + 1;
        OptionalInt opMaxSize = this.getMaxSize();
        if (opMaxSize.isEmpty()) {
            return;
        }
        if (opMaxSize.getAsInt() < size) {
            throw new MoveException(context, AdventureMessageConfig.ERROR_OVERSIZED,
                                    new AbstractMap.SimpleEntry<>(vessel, size - opMaxSize.getAsInt()));
        }
    }

    @Override
    public void onProcessRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {

    }

    @Override
    public @NotNull MaxSizeRequirement getRequirementsBetween(@NotNull MaxSizeRequirement requirement) {
        MaxSizeRequirement maxSizeRequirement = this;
        Integer amount = null;
        while (maxSizeRequirement != null && maxSizeRequirement != requirement) {
            if (maxSizeRequirement.maxSize != null) {
                amount = maxSizeRequirement.maxSize;
            }
            maxSizeRequirement = maxSizeRequirement.getParent().orElse(null);
        }
        return new MaxSizeRequirement(requirement, amount);
    }

    @Override
    public @NotNull MaxSizeRequirement createChild() {
        return new MaxSizeRequirement(this);
    }

    public MaxSizeRequirement createChild(@Nullable Integer value) {
        return new MaxSizeRequirement(this, value);
    }


    @Override
    public @NotNull MaxSizeRequirement createCopy() {
        return new MaxSizeRequirement(this.parent, this.maxSize);
    }

    public @NotNull MaxSizeRequirement createCopy(Integer value) {
        return new MaxSizeRequirement(this.parent, value);
    }

    @Override
    public Optional<MaxSizeRequirement> getParent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public boolean isEnabled() {
        OptionalInt opSize = this.getMaxSize();
        return opSize.isPresent() && opSize.getAsInt() != 0 && opSize.getAsInt() != Integer.MAX_VALUE;
    }

    @Override
    public void serialize(@NotNull ConfigurationStream stream, boolean withParentData) {
        if (withParentData) {
            this.getMaxSize().ifPresent(value -> stream.set(AbstractShipType.MAX_SIZE, value));
            return;
        }
        if (this.maxSize != null) {
            stream.set(AbstractShipType.MAX_SIZE, this.maxSize);
        }
    }
}
