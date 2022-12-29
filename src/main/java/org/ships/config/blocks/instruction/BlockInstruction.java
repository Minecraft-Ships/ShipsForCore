package org.ships.config.blocks.instruction;

import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;

public interface BlockInstruction {

    @NotNull BlockType getType();

    OptionalInt getBlockLimit();

    @NotNull CollideType getCollide();

}
