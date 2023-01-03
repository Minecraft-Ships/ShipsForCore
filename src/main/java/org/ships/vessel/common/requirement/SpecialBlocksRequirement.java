package org.ships.vessel.common.requirement;

import org.core.config.ConfigurationStream;
import org.core.utils.Identifiable;
import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.config.messages.messages.error.data.RequirementPercentMessageData;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlock;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.TreeSet;

public class SpecialBlocksRequirement implements Requirement<SpecialBlocksRequirement> {

    private final @Nullable SpecialBlocksRequirement parent;
    private final @Nullable Collection<BlockType> specialBlocks;
    private final @Nullable Float specialBlocksPercentage;

    public SpecialBlocksRequirement(@NotNull SpecialBlocksRequirement parent) {
        this(parent, null, null);
    }

    public SpecialBlocksRequirement(@Nullable SpecialBlocksRequirement parent,
                                    @Nullable Float specialBlocksPercent,
                                    @Nullable Collection<BlockType> specialBlocks) {
        if (parent == null && (specialBlocks == null || specialBlocksPercent == null)) {
            throw new IllegalArgumentException("Parent cannot be null if another value is null");
        }
        if (specialBlocksPercent != null && (specialBlocksPercent < 0 || specialBlocksPercent > 100)) {
            throw new IndexOutOfBoundsException("Percentage can only be between 0 and 100");
        }
        this.parent = parent;
        this.specialBlocks = specialBlocks;
        this.specialBlocksPercentage = specialBlocksPercent;
    }

    public Optional<Float> getSpecifiedPercent() {
        return Optional.ofNullable(this.specialBlocksPercentage);
    }

    public Collection<BlockType> getSpecifiedBlocks() {
        if (this.specialBlocks != null) {
            return this.specialBlocks;
        }
        if (this.parent == null) {
            throw new RuntimeException("You skipped the constructor checks");
        }
        return this.parent.getSpecifiedBlocks();
    }

    public boolean isPercentageSpecified() {
        return this.specialBlocksPercentage != null;
    }

    public boolean isBlocksSpecified() {
        return this.specialBlocks != null;
    }

    public float getPercentage() {
        if (this.specialBlocksPercentage != null) {
            return this.specialBlocksPercentage;
        }
        if (this.parent == null) {
            throw new RuntimeException("You skipped the constructor checks");
        }
        return this.parent.getPercentage();
    }

    public @NotNull Collection<BlockType> getBlocks() {
        if (this.specialBlocks != null) {
            return this.specialBlocks;
        }
        if (this.parent == null) {
            throw new RuntimeException("You skipped the constructor checks");
        }
        return this.parent.getBlocks();
    }


    @Override
    public boolean useOnStrict() {
        return false;
    }

    @Override
    public void onCheckRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {
        Collection<BlockType> specialBlocks = new TreeSet<>(Comparator.comparing(Identifiable::getName));
        specialBlocks.addAll(this.getBlocks());
        float percentageRequired = this.getPercentage();
        long blocksFound = context
                .getMovingStructure()
                .parallelStream()
                .map(MovingBlock::getStoredBlockData)
                .filter(block -> specialBlocks.contains(block.getType()))
                .count();
        if (blocksFound == 0) {
            throw new MoveException(context, AdventureMessageConfig.ERROR_SPECIAL_BLOCK_PERCENT_NOT_ENOUGH,
                                    new RequirementPercentMessageData(vessel, 0, 0));
        }

        double totalPercent = (blocksFound * 100.0) / context.getMovingStructure().size();
        if (totalPercent < percentageRequired) {
            throw new MoveException(context, AdventureMessageConfig.ERROR_SPECIAL_BLOCK_PERCENT_NOT_ENOUGH,
                                    new RequirementPercentMessageData(vessel, totalPercent, (int) blocksFound));
        }
    }

    @Override
    public void onProcessRequirement(@NotNull MovementContext context, @NotNull Vessel vessel) throws MoveException {

    }

    @Override
    public @NotNull SpecialBlocksRequirement getRequirementsBetween(@NotNull SpecialBlocksRequirement requirement) {
        SpecialBlocksRequirement specialBlock = this;
        Collection<BlockType> type = null;
        Float amount = null;
        while (specialBlock != null && specialBlock != requirement) {
            if (specialBlock.specialBlocks != null) {
                type = specialBlock.specialBlocks;
            }
            if (specialBlock.specialBlocksPercentage != null) {
                amount = specialBlock.specialBlocksPercentage;
            }
            specialBlock = specialBlock.getParent().orElse(null);
        }
        return new SpecialBlocksRequirement(requirement, amount, type);
    }

    @Override
    public @NotNull SpecialBlocksRequirement createChild() {
        return new SpecialBlocksRequirement(this);
    }

    public @NotNull SpecialBlocksRequirement createChildWithPercentage(@Nullable Float value) {
        return new SpecialBlocksRequirement(this, value, null);
    }

    public @NotNull SpecialBlocksRequirement createChildWithBlocks(@Nullable Collection<BlockType> blocks) {
        return new SpecialBlocksRequirement(this, null, blocks);
    }

    @Override
    public @NotNull SpecialBlocksRequirement createCopy() {
        return new SpecialBlocksRequirement(this.parent, this.specialBlocksPercentage, this.specialBlocks);
    }

    public @NotNull SpecialBlocksRequirement createCopyWithPercentage(@Nullable Float value) {
        return new SpecialBlocksRequirement(this.parent, value, this.specialBlocks);
    }

    public @NotNull SpecialBlocksRequirement createCopyWithBlocks(@Nullable Collection<BlockType> blocks) {
        return new SpecialBlocksRequirement(this.parent, this.specialBlocksPercentage, blocks);
    }


    @Override
    public Optional<SpecialBlocksRequirement> getParent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public boolean isEnabled() {
        if (this.getBlocks().isEmpty()) {
            return false;
        }
        return this.getPercentage() != 0;
    }

    @Override
    public void serialize(@NotNull ConfigurationStream stream, boolean withParentData) {
        if (withParentData) {
            this.serializeInstance(stream);
            return;
        }
        this.serializeParent(stream);
    }

    private void serializeParent(@NotNull ConfigurationStream stream) {
        float percent = this.getPercentage();
        Collection<BlockType> blocks = this.getBlocks();

        stream.set(AbstractShipType.SPECIAL_BLOCK_PERCENT, percent);
        stream.set(AbstractShipType.SPECIAL_BLOCK_TYPE, blocks);
    }

    private void serializeInstance(@NotNull ConfigurationStream stream) {
        Optional<Float> opPercent = this.getSpecifiedPercent();
        opPercent.ifPresent(value -> stream.set(AbstractShipType.SPECIAL_BLOCK_PERCENT, value));
        if (this.specialBlocks != null) {
            stream.set(AbstractShipType.SPECIAL_BLOCK_TYPE, this.specialBlocks);
        }
    }
}
