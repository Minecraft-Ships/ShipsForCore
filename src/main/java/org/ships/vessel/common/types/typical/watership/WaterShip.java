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
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.exceptions.MoveException;
import org.ships.exceptions.NoLicencePresent;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlock;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.movement.result.data.RequiredPercentMovementData;
import org.ships.vessel.common.assits.Fallable;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.assits.WaterType;
import org.ships.vessel.common.flag.AltitudeLockFlag;
import org.ships.vessel.common.requirement.Requirement;
import org.ships.vessel.common.requirement.SpecialBlocksRequirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;

public class WaterShip extends AbstractShipsVessel implements WaterType, Fallable, VesselRequirement {

    protected final ConfigurationNode.KnownParser.SingleKnown<Double> configSpecialBlockPercent =
            new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_DOUBLE, "Block", "Special", "Percent");
    protected final ConfigurationNode.KnownParser.CollectionKnown<BlockType> configSpecialBlockType =
            new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_BLOCK_TYPE, "Block", "Special",
                    "Type");
    protected @Deprecated Float specialBlockPercent;
    protected @Deprecated Set<BlockType> specialBlocks = ShipType.WATERSHIP.getDefaultSpecialBlockType();

    private Collection<Requirement> requirements = new HashSet<>();

    public WaterShip(ShipType<WaterShip> type, LiveTileEntity licence) throws NoLicencePresent {
        super(licence, type);
        this.flags.add(new AltitudeLockFlag(true));
        this.initRequirements();
    }

    public WaterShip(ShipType<WaterShip> type, SignTileEntity ste, SyncBlockPosition position) {
        super(ste, position, type);
        this.flags.add(new AltitudeLockFlag(true));
        this.initRequirements();
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
    public @NotNull Collection<Requirement> getRequirements() {
        return Collections.unmodifiableCollection(this.requirements);
    }

    @Override
    public void meetsRequirements(MovementContext context) throws MoveException {
        VesselRequirement.super.meetsRequirements(context);
        if (!context.isStrictMovement()) {
            return;
        }
        Optional<Integer> opWaterLevel = this.getWaterLevel(MovingBlock::getAfterPosition,
                context.getMovingStructure());
        if (opWaterLevel.isEmpty()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NO_MOVING_TO_FOUND,
                    Collections.singletonList(BlockTypes.WATER)));
        }
        int specialBlockCount = 0;
        for (MovingBlock movingBlock : context.getMovingStructure()) {
            SyncBlockPosition blockPosition = movingBlock.getBeforePosition();
            if (this.getSpecialBlocks().stream().anyMatch(b -> b.equals(blockPosition.getBlockType()))) {
                specialBlockCount++;
            }
        }
        float specialBlockPercent = ((specialBlockCount * 100.0f) / (context
                .getMovingStructure()
                .stream()
                .filter(m -> !m.getStoredBlockData().getType().equals(BlockTypes.AIR))
                .count()));
        if ((this.getSpecialBlockPercent() != 0) && specialBlockPercent <= this.getSpecialBlockPercent()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_PERCENT,
                    new RequiredPercentMovementData(this.getSpecialBlocks().iterator().next(),
                            this.getSpecialBlockPercent(), specialBlockPercent)));
        }
    }

    @Override
    public void setRequirement(Requirement updated) {
        this.requirements
                .parallelStream()
                .filter(r -> r.getClass().getName().equals(updated.getClass().getName()))
                .findAny()
                .ifPresent(requ -> this.requirements.remove(requ));
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
    public AbstractShipsVessel deserializeExtra(ConfigurationStream file) {
        file.getDouble(this.configSpecialBlockPercent).ifPresent(v -> this.specialBlockPercent = v.floatValue());
        this.specialBlocks = file.parseCollection(this.configSpecialBlockType, new HashSet<>());
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

    @Override
    public boolean shouldFall() {
        int specialBlockCount = 0;
        boolean inWater = false;
        for (SyncBlockPosition blockPosition : this.getStructure().getSyncedPositions()) {
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
        float specialBlockPercent = ((specialBlockCount * 100.0f) /
                this.getStructure().getOriginalRelativePositions().size());
        return (!(this.getSpecialBlockPercent() == 0) || !(specialBlockPercent <= this.getSpecialBlockPercent()));
    }
}
