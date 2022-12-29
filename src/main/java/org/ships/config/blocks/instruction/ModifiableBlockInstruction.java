package org.ships.config.blocks.instruction;

import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public class ModifiableBlockInstruction implements BlockInstruction {

    private final @NotNull BlockType type;
    private @NotNull CollideType collide;
    private @Nullable Integer blockLimit;

    public ModifiableBlockInstruction(@NotNull BlockType type) {
        this(type, CollideType.DETECT_COLLIDE, null);
    }

    public ModifiableBlockInstruction(@NotNull BlockType type, @NotNull CollideType collide, @Nullable Integer value) {
        this.type = type;
        this.collide = collide;
        this.blockLimit = value;
    }

    public ModifiableBlockInstruction setCollide(@NotNull CollideType type) {
        this.collide = type;
        return this;
    }

    public ModifiableBlockInstruction removeBlockLimit() {
        return this.setBlockLimit(null);
    }

    public ModifiableBlockInstruction setBlockLimit(@Nullable Integer value) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException("Cannot be negative");
        }
        this.blockLimit = value;
        return this;
    }

    @Override
    public @NotNull BlockType getType() {
        return this.type;
    }

    @Override
    public OptionalInt getBlockLimit() {
        if (this.blockLimit == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(this.blockLimit);
    }

    @Override
    public @NotNull CollideType getCollide() {
        return this.collide;
    }
}
