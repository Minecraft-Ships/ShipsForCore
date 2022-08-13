package org.ships.vessel.common.types.typical.marsship;

import org.array.utils.ArrayUtils;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.exceptions.MoveException;
import org.ships.exceptions.NoLicencePresent;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlock;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.movement.result.data.RequiredPercentMovementData;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.requirement.Requirement;
import org.ships.vessel.common.requirement.SpecialBlocksRequirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;

public class Marsship extends AbstractShipsVessel implements AirType, VesselRequirement {

    protected final ConfigurationNode.KnownParser.SingleKnown<Double> configSpecialBlockPercent =
            new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_DOUBLE, "Block", "Special", "Percent");
    protected final ConfigurationNode.KnownParser.CollectionKnown<BlockType> configSpecialBlockType =
            new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_BLOCK_TYPE, "Block", "Special",
                    "Type");
    protected @Deprecated
    @Nullable Float specialBlockPercent;
    protected @Deprecated
    @Nullable Collection<BlockType> specialBlocks;

    private Collection<Requirement> requirements = new HashSet<>();


    public Marsship(ShipType<? extends Marsship> type, LiveTileEntity licence) throws NoLicencePresent {
        super(licence, type);
    }

    public Marsship(ShipType<? extends Marsship> type, SignTileEntity ste, SyncBlockPosition position) {
        super(ste, position, type);
    }

    public SpecialBlocksRequirement getSpecialBlocksRequirement() {
        return this
                .getRequirement(SpecialBlocksRequirement.class)
                .orElseThrow(() -> new RuntimeException("Marsship is missing the special blocks requirement"));
    }

    @Deprecated(forRemoval = true)
    public float getSpecialBlockPercent() {
        return this.getSpecialBlocksPercent();
    }

    public Collection<BlockType> getSpecialBlocks() {
        return this.getSpecialBlocksRequirement().getBlocks();
    }

    public void setSpecialBlocks(@Nullable Collection<BlockType> types) {
        SpecialBlocksRequirement requirement = this.getSpecialBlocksRequirement();
        SpecialBlocksRequirement requirementCopy = requirement.createCopyWithBlocks(types);
        this.setRequirement(requirementCopy);
    }

    public float getSpecialBlocksPercent() {
        return this.getSpecialBlocksRequirement().getPercentage();
    }

    public void setSpecialBlocksPercent(@Nullable Float value) {
        SpecialBlocksRequirement requirement = this.getSpecialBlocksRequirement();
        SpecialBlocksRequirement requirementCopy = requirement.createCopyWithPercentage(value);
        this.setRequirement(requirementCopy);
    }

    @Override
    public Collection<Requirement> getRequirements() {
        return Collections.unmodifiableCollection(this.requirements);
    }

    public boolean isSpecialBlocksSpecified() {
        return this.getSpecialBlocksRequirement().isBlocksSpecified();
    }

    public boolean isSpecialBlocksPercentSpecified() {
        return this.getSpecialBlocksRequirement().isPercentageSpecified();
    }

    @Override
    public @NotNull MarsshipType getType() {
        return (MarsshipType) super.getType();
    }

    @Override
    public @NotNull Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Special Block",
                ArrayUtils.toString(", ", Parser.STRING_TO_BLOCK_TYPE::unparse, this.getSpecialBlocks()));
        map.put("Required Percent", this.getSpecialBlockPercent() + "");
        return map;
    }

    @Override
    public @NotNull Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(ConfigurationStream file) {
        Map<ConfigurationNode.KnownParser<?, ?>, Object> map = new HashMap<>();
        map.put(this.configSpecialBlockType, this.getSpecialBlocks());
        map.put(this.configSpecialBlockPercent, this.getSpecialBlockPercent());
        return map;
    }

    @Override
    public AbstractShipsVessel deserializeExtra(ConfigurationStream file) {
        file.getDouble(this.configSpecialBlockPercent).ifPresent(v -> this.specialBlockPercent = v.floatValue());
        this.specialBlocks = file.parseCollection(this.configSpecialBlockType, new HashSet<>());
        return this;
    }

    @Override
    public void meetsRequirements(MovementContext context) throws MoveException {
        VesselRequirement.super.meetsRequirements(context);
        if (!context.isStrictMovement()) {
            return;
        }
        int specialBlocks = 0;
        for (MovingBlock block : context.getMovingStructure()) {
            BlockDetails details = block.getStoredBlockData();
            if (this.getSpecialBlocks().stream().anyMatch(b -> b.equals(details.getType()))) {
                specialBlocks++;
            }
        }
        float specialBlockPercent = ((specialBlocks * 100.0f) / context
                .getMovingStructure()
                .stream()
                .filter(m -> !m.getStoredBlockData().getType().equals(BlockTypes.AIR))
                .count());
        if ((this.getSpecialBlockPercent() != 0) && specialBlockPercent <= this.getSpecialBlockPercent()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_PERCENT,
                    new RequiredPercentMovementData(this.getSpecialBlocks().iterator().next(),
                            this.getSpecialBlockPercent(), specialBlockPercent)));
        }
    }

    @Override
    public void setRequirement(Requirement updated) {
        this.getRequirement(updated.getClass()).ifPresent(req -> this.requirements.remove(req));
        this.requirements.add(updated);
    }

}
