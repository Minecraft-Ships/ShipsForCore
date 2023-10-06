package org.ships.vessel.common.types.typical.watership;

import org.array.utils.ArrayUtils;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.exceptions.NoLicencePresent;
import org.ships.vessel.common.assits.Fallable;
import org.ships.vessel.common.assits.FileBasedVessel;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.assits.WaterType;
import org.ships.vessel.common.flag.AltitudeLockFlag;
import org.ships.vessel.common.requirement.MaxSizeRequirement;
import org.ships.vessel.common.requirement.MinSizeRequirement;
import org.ships.vessel.common.requirement.Requirement;
import org.ships.vessel.common.requirement.SpecialBlocksRequirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipType;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;

public class WaterShip extends AbstractShipsVessel implements WaterType, Fallable, VesselRequirement {

    protected final ConfigurationNode.KnownParser.SingleKnown<Double> configSpecialBlockPercent = new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_DOUBLE, "Block", "Special", "Percent");
    protected final ConfigurationNode.KnownParser.CollectionKnown<BlockType> configSpecialBlockType = new ConfigurationNode.KnownParser.CollectionKnown<>(
            Parser.STRING_TO_BLOCK_TYPE, "Block", "Special", "Type");

    private final Collection<Requirement<?>> requirements = new HashSet<>();

    @Deprecated(forRemoval = true)
    public WaterShip(ShipType<WaterShip> type, LiveTileEntity licence) throws NoLicencePresent {
        super(licence, type);
        this.flags.add(new AltitudeLockFlag(true));
        this.initRequirements();
    }

    @Deprecated(forRemoval = true)
    public WaterShip(ShipType<WaterShip> type, SignTileEntity ste, SyncBlockPosition position) {
        super(ste, position, type);
        this.flags.add(new AltitudeLockFlag(true));
        this.initRequirements();
    }

    public WaterShip(@NotNull LiveSignTileEntity licence,
                     boolean isFrontOfSign,
                     @NotNull ShipType<? extends AbstractShipsVessel> type) {
        super(licence, isFrontOfSign, type);
    }

    public WaterShip(SignSide signSide, SyncBlockPosition position, ShipType<? extends AbstractShipsVessel> type) {
        super(signSide, position, type);
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

    public float getSpecialBlockPercent() {
        return this.getSpecialBlocksRequirement().getPercentage();
    }

    public @NotNull SpecialBlocksRequirement getSpecialBlocksRequirement() {
        return this
                .getRequirement(SpecialBlocksRequirement.class)
                .orElseThrow(() -> new RuntimeException("Watership does not have a requirement for special blocks"));
    }

    public @NotNull Collection<BlockType> getSpecialBlocks() {
        return this.getSpecialBlocksRequirement().getBlocks();
    }

    @Override
    public @NotNull WaterShipType getType() {
        return (WaterShipType) super.getType();
    }

    @Override
    public @NotNull Collection<Requirement<?>> getRequirements() {
        return Collections.unmodifiableCollection(this.requirements);
    }

    @Override
    public void setRequirement(@NotNull Requirement<?> updated) {
        this.requirements
                .parallelStream()
                .filter(r -> r.getClass().getName().equals(updated.getClass().getName()))
                .findAny()
                .ifPresent(this.requirements::remove);
        this.requirements.add(updated);
    }

    @Override
    public Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(ConfigurationStream file) {
        Map<ConfigurationNode.KnownParser<?, ?>, Object> map = new HashMap<>();
        map.put(this.configSpecialBlockType, this.getSpecialBlocks());
        map.put(this.configSpecialBlockPercent, this.getSpecialBlockPercent());
        return map;
    }

    @Override
    public @NotNull FileBasedVessel deserializeExtra(@NotNull ConfigurationStream file) {
        MaxSizeRequirement maxSizeRequirement = this.getMaxBlocksRequirement();
        Optional<Integer> opMaxSize = file.getInteger(MaxSizeRequirement.MAX_SIZE);
        if (opMaxSize.isPresent()) {
            maxSizeRequirement = maxSizeRequirement.createChild(opMaxSize.get());
        }
        this.setRequirement(maxSizeRequirement);

        MinSizeRequirement minSizeRequirement = this.getMinBlocksRequirement();
        Optional<Integer> opMinSize = file.getInteger(MinSizeRequirement.MIN_SIZE);
        if (opMinSize.isPresent()) {
            minSizeRequirement = minSizeRequirement.createChild(opMinSize.get());
        }
        this.setRequirement(minSizeRequirement);

        SpecialBlocksRequirement specialRequirement = this.getSpecialBlocksRequirement();
        Optional<Double> opPercent = file.getDouble(AbstractShipType.SPECIAL_BLOCK_PERCENT);
        if (opPercent.isPresent()) {
            specialRequirement = specialRequirement.createChildWithPercentage(opPercent.get().floatValue());
        }
        HashSet<BlockType> collection = file.parseCollection(AbstractShipType.SPECIAL_BLOCK_TYPE, new HashSet<>());
        if (!collection.isEmpty()) {
            specialRequirement = specialRequirement.createChildWithBlocks(collection);
        }
        this.setRequirement(specialRequirement);
        return this;
    }


    @Override
    public @NotNull Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Special Block",
                ArrayUtils.toString(", ", Parser.STRING_TO_BLOCK_TYPE::unparse, this.getSpecialBlocks()));
        map.put("Required Percent", this.getSpecialBlockPercent() + "");
        return map;
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
    public boolean shouldFall() {
        int specialBlockCount = 0;
        boolean inWater = false;
        for (SyncBlockPosition blockPosition : this.getStructure().getSyncedPositionsRelativeToWorld()) {
            if (this.getSpecialBlocks().stream().anyMatch(b -> b.equals(blockPosition.getBlockType()))) {
                specialBlockCount++;
            }
            for (Direction direction : Direction.withYDirections(FourFacingDirection.getFourFacingDirections())) {
                if (BlockTypes.WATER.equals(blockPosition.getRelative(direction).getBlockType())) {
                    inWater = true;
                }
            }
        }
        if (!inWater) {
            return true;
        }
        float specialBlockPercent = ((specialBlockCount * 100.0f) / this
                .getStructure()
                .getOriginalRelativePositionsToCenter()
                .size());
        return (!(this.getSpecialBlockPercent() == 0) || !(specialBlockPercent <= this.getSpecialBlockPercent()));
    }
}
