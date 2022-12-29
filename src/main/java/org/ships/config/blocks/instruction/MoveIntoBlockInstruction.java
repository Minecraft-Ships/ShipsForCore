package org.ships.config.blocks.instruction;

import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;

public class MoveIntoBlockInstruction implements BlockInstruction {

    private @NotNull BlockType type;

    public MoveIntoBlockInstruction(@NotNull BlockType type) {
        this.type = type;
    }

    @Override
    public @NotNull BlockType getType() {
        return this.type;
    }

    @Override
    public OptionalInt getBlockLimit() {
        return OptionalInt.empty();
    }

    @Override
    public @NotNull CollideType getCollide() {
        return CollideType.IGNORE;
    }
}
