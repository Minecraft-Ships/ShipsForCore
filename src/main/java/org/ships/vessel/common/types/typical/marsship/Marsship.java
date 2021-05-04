package org.ships.vessel.common.types.typical.marsship;

import org.array.utils.ArrayUtils;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlock;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.movement.result.data.RequiredPercentMovementData;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Marsship extends AbstractShipsVessel implements AirType, org.ships.vessel.common.assits.VesselRequirement {

    protected Float specialBlockPercent;
    protected Set<BlockType> specialBlocks = new HashSet<>();

    protected ConfigurationNode.KnownParser.SingleKnown<Double> configSpecialBlockPercent = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_DOUBLE, "Block", "Special", "Percent");
    protected ConfigurationNode.KnownParser.CollectionKnown<BlockType, Set<BlockType>> configSpecialBlockType = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_BLOCK_TYPE, "Block", "Special", "Type");


    public Marsship(MarsshipType type, LiveSignTileEntity licence) {
        super(licence, type);
    }

    public Marsship(MarsshipType type, SignTileEntity ste, SyncBlockPosition position) {
        super(ste, position, type);
    }

    public float getSpecialBlockPercent() {
        if (this.specialBlockPercent == null) {
            return this.getType().getDefaultSpecialBlockPercent();
        }
        return this.specialBlockPercent;
    }

    public Set<BlockType> getSpecialBlocks() {
        if (this.specialBlocks.isEmpty()) {
            return this.getType().getDefaultSpecialBlockType();
        }
        return this.specialBlocks;
    }

    public MarsshipType getType() {
        return (MarsshipType) super.getType();
    }

    @Override
    public Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Special Block", ArrayUtils.toString(", ", Parser.STRING_TO_BLOCK_TYPE::unparse, this.getSpecialBlocks()));
        map.put("Required Percent", this.getSpecialBlockPercent() + "");
        return map;
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
        float specialBlockPercent = ((specialBlocks * 100.0f) / context.getMovingStructure().stream().filter(m -> !m.getStoredBlockData().getType().equals(BlockTypes.AIR)).count());
        if ((this.getSpecialBlockPercent() != 0) && specialBlockPercent <= this.getSpecialBlockPercent()) {
            throw new MoveException(new AbstractFailedMovement<>(this, MovementResult.NOT_ENOUGH_PERCENT, new RequiredPercentMovementData(this.getSpecialBlocks().iterator().next(), this.getSpecialBlockPercent(), specialBlockPercent)));
        }
    }

    @Override
    public void processRequirements(MovementContext context) throws MoveException {
        VesselRequirement.super.processRequirements(context);
    }
}
