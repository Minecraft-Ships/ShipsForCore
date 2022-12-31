package org.ships.vessel.common.types.typical.marsship;

import org.array.utils.ArrayUtils;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.exceptions.NoLicencePresent;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.requirement.MaxSizeRequirement;
import org.ships.vessel.common.requirement.MinSizeRequirement;
import org.ships.vessel.common.requirement.Requirement;
import org.ships.vessel.common.requirement.SpecialBlocksRequirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;

public class Marsship extends AbstractShipsVessel implements AirType, VesselRequirement {

    protected final ConfigurationNode.KnownParser.SingleKnown<Double> configSpecialBlockPercent = new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_DOUBLE, "Block", "Special", "Percent");
    protected final ConfigurationNode.KnownParser.CollectionKnown<BlockType> configSpecialBlockType = new ConfigurationNode.KnownParser.CollectionKnown<>(
            Parser.STRING_TO_BLOCK_TYPE, "Block", "Special", "Type");

    private final Collection<Requirement<?>> requirements = new HashSet<>();


    public Marsship(ShipType<? extends Marsship> type, LiveTileEntity licence) throws NoLicencePresent {
        super(licence, type);
        this.initRequirements();
    }

    public Marsship(ShipType<? extends Marsship> type, SignTileEntity ste, SyncBlockPosition position) {
        super(ste, position, type);
        this.initRequirements();
    }

    public MaxSizeRequirement getMaxBlocksRequirement() {
        return this
                .getRequirement(MaxSizeRequirement.class)
                .orElseThrow(() -> new RuntimeException("Submarine is missing a max blocks requirement"));
    }

    public MinSizeRequirement getMinBlocksRequirement() {
        return this
                .getRequirement(MinSizeRequirement.class)
                .orElseThrow(() -> new RuntimeException("Submarine is missing a min blocks requirement"));
    }

    public SpecialBlocksRequirement getSpecialBlocksRequirement() {
        return this
                .getRequirement(SpecialBlocksRequirement.class)
                .orElseThrow(() -> new RuntimeException("Marsship is missing the special blocks requirement"));
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
    public Collection<Requirement<?>> getRequirements() {
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

    public @NotNull Vessel setMaxSize(@Nullable Integer size) {
        MaxSizeRequirement maxRequirements = this.getMaxBlocksRequirement();
        maxRequirements = maxRequirements.createCopy(size);
        this.setRequirement(maxRequirements);
        return this;
    }

    public boolean isMaxSizeSpecified() {
        return this.getMaxBlocksRequirement().isMaxSizeSpecified();
    }

    public @NotNull Vessel setMinSize(@Nullable Integer size) {
        MinSizeRequirement minRequirements = this.getMinBlocksRequirement();
        minRequirements = minRequirements.createCopy(size);
        this.setRequirement(minRequirements);
        return this;
    }

    public boolean isMinSizeSpecified() {
        return this.getMinBlocksRequirement().isMinSizeSpecified();
    }

    @Override
    public @NotNull Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Special Block",
                ArrayUtils.toString(", ", Parser.STRING_TO_BLOCK_TYPE::unparse, this.getSpecialBlocks()));
        map.put("Required Percent", this.getSpecialBlocksPercent() + "");
        return map;
    }

    @Override
    public @NotNull Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(ConfigurationStream file) {
        Map<ConfigurationNode.KnownParser<?, ?>, Object> map = new HashMap<>();
        map.put(this.configSpecialBlockType, this.getSpecialBlocks());
        map.put(this.configSpecialBlockPercent, this.getSpecialBlocksPercent());
        return map;
    }

    @Override
    public AbstractShipsVessel deserializeExtra(ConfigurationStream file) {
        SpecialBlocksRequirement requirements = this.getSpecialBlocksRequirement();
        Optional<Double> opSpecialBlockPercent = file.getDouble(this.configSpecialBlockPercent);
        if (opSpecialBlockPercent.isPresent()) {
            requirements = requirements.createCopyWithPercentage(opSpecialBlockPercent.get().floatValue());
        }
        requirements = requirements.createChildWithBlocks(
                file.parseCollection(this.configSpecialBlockType, new HashSet<>(), null));
        this.setRequirement(requirements);
        return this;
    }

    @Override
    public void setRequirement(Requirement<?> updated) {
        this.getRequirement(updated.getClass()).ifPresent(this.requirements::remove);
        this.requirements.add(updated);
    }

}
