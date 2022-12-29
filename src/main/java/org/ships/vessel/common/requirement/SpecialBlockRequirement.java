package org.ships.vessel.common.requirement;

import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.config.messages.messages.error.data.NamedBlockMessageData;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;
import java.util.OptionalInt;

public class SpecialBlockRequirement implements Requirement<SpecialBlockRequirement> {

    private final @Nullable BlockType blockType;
    private final @Nullable Integer amount;
    private final @Nullable SpecialBlockRequirement parent;
    private final @Nullable String displayName;

    public SpecialBlockRequirement(@NotNull SpecialBlockRequirement parent, @Nullable String name) {
        this(parent, null, null, name);
    }

    public SpecialBlockRequirement(@Nullable SpecialBlockRequirement parent,
                                   @Nullable BlockType type,
                                   @Nullable Integer amount,
                                   @Nullable String name) {
        if (parent == null && (type == null || amount == null)) {
            throw new IllegalArgumentException("parent cannot be null if another value is");
        }
        this.amount = amount;
        this.blockType = type;
        this.parent = parent;
        this.displayName = name;
    }

    public Optional<String> getSpecifiedDisplayName(){
        return Optional.ofNullable(this.displayName);
    }

    public Optional<String> getDisplayName() {
        if (this.parent == null) {
            return Optional.ofNullable(this.displayName);
        }
        return this.parent.getDisplayName();
    }

    public OptionalInt getSpecifiedAmount(){
        if(this.amount == null){
            return OptionalInt.empty();
        }
        return OptionalInt.of(this.amount);
    }

    public int getAmount() {
        if (this.parent == null) {
            if (this.amount == null) {
                throw new RuntimeException("You skipped the constructor checks");
            }
            return this.amount;
        }
        return this.parent.getAmount();
    }

    public Optional<BlockType> getSpecifiedBlock(){
        return Optional.ofNullable(this.blockType);
    }

    public @NotNull BlockType getBlock() {
        if (this.parent == null) {
            if (this.blockType == null) {
                throw new RuntimeException("You skipped the constructor checks");
            }
            return this.blockType;
        }
        return this.parent.getBlock();
    }

    @Override
    public boolean useOnStrict() {
        return false;
    }

    @Override
    public void onCheckRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {
        BlockType requiredType = this.getBlock();
        int amount = this.getAmount();

        long found = context
                .getMovingStructure()
                .parallelStream()
                .map(moving -> moving.getStoredBlockData().getType().equals(requiredType))
                .count();
        if (found < amount) {
            throw new MoveException(context, AdventureMessageConfig.ERROR_FAILED_TO_FIND_NAMED_BLOCK,
                                    new NamedBlockMessageData()
                                            .setVessel(vessel)
                                            .setType(requiredType)
                                            .setNamedBlock(this.displayName));
        }
    }

    @Override
    public void onProcessRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {

    }

    @Override
    public @NotNull SpecialBlockRequirement getRequirementsBetween(@NotNull SpecialBlockRequirement requirement) {
        SpecialBlockRequirement specialBlock = this;
        BlockType type = null;
        Integer amount = null;
        String displayName = null;
        while (specialBlock != null && specialBlock != requirement) {
            if (specialBlock.blockType != null) {
                type = specialBlock.blockType;
            }
            if (specialBlock.amount != null) {
                amount = specialBlock.amount;
            }
            if (specialBlock.displayName != null) {
                displayName = specialBlock.displayName;
            }
            specialBlock = specialBlock.getParent().orElse(null);
        }
        return new SpecialBlockRequirement(requirement, type, amount, displayName);
    }

    @Override
    public @NotNull SpecialBlockRequirement createChild() {
        return new SpecialBlockRequirement(this, this.displayName);
    }

    @Override
    public @NotNull SpecialBlockRequirement createCopy() {
        return new SpecialBlockRequirement(this.parent, this.blockType, this.amount, this.displayName);
    }

    public @NotNull SpecialBlockRequirement createCopyWithAmount(@Nullable Integer amount) {
        return new SpecialBlockRequirement(this.parent, this.blockType, amount, this.displayName);
    }

    @Override
    public Optional<SpecialBlockRequirement> getParent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public boolean isEnabled() {
        return this.getAmount() != 0;
    }
}
