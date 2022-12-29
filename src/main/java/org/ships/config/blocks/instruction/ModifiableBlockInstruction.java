package org.ships.config.blocks.instruction;

import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;

public class ModifiableBlockInstruction implements BlockInstruction {

    private @NotNull BlockType type;
    private @NotNull CollideType collide;
    private @Nullable Integer blockLimit;

    public ModifiableBlockInstruction(@NotNull BlockType type, @NotNull CollideType collide, @Nullable Integer value){
        this.type = type;
        this.collide = collide;
        this.blockLimit = value;
    }

    @Override
    public @NotNull BlockType getType() {
        return this.type;
    }

    @Override
    public OptionalInt getBlockLimit() {
        if(this.blockLimit == null){
            return OptionalInt.empty();
        }
        return OptionalInt.of(this.blockLimit);
    }

    @Override
    public @NotNull CollideType getCollide() {
        return this.collide;
    }
}
