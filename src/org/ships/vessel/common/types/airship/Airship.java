package org.ships.vessel.common.types.airship;

import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.movement.result.data.RequiredPercentMovementData;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.types.AbstractShipsVessel;
import org.ships.vessel.common.types.ShipType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Airship extends AbstractShipsVessel implements AirType {

    protected boolean useBurner = true;
    protected float specialBlockPercent = 60;
    protected BlockType specialBlock = BlockTypes.WOOL_WHITE;

    protected ConfigurationNode configBurnerBlock = new ConfigurationNode("Block", "Burner");
    protected ConfigurationNode configSpecialBlockPercent = new ConfigurationNode("Block", "Special", "Percent");
    protected ConfigurationNode configSpecialBlockType = new ConfigurationNode("Block", "Special", "Type");

    public Airship(LiveSignTileEntity licence) {
        super(licence);
    }

    public Airship(SignTileEntity ste, BlockPosition position){
        super(ste, position);
    }

    @Override
    public void meetsRequirement(MovingBlockSet movingBlocks) throws MoveException{
        int specialBlockCount = 0;
        boolean burnerFound = false;
        for(MovingBlock movingBlock : movingBlocks){
            BlockPosition blockPosition = movingBlock.getBeforePosition();
            if(blockPosition.getBlockType().equals(BlockTypes.FIRE)){
                burnerFound = true;
            }
            if(blockPosition.getBlockType().equals(this.specialBlock)){
                specialBlockCount++;
            }
        }
        if(this.useBurner && burnerFound){
            throw new MoveException(new AbstractFailedMovement(this, MovementResult.NO_BURNER_FOUND, false));
        }
        float specialBlockPercent = ((specialBlockCount * 100.0f)/movingBlocks.size());
        if((this.specialBlockPercent != 0) && specialBlockCount >= this.specialBlockPercent){
            throw new MoveException(new AbstractFailedMovement(this, MovementResult.NOT_ENOUGH_PERCENT, new RequiredPercentMovementData(this.specialBlock, this.specialBlockPercent, specialBlockPercent)));
        }
    }

    @Override
    public Map<ConfigurationNode, Object> serialize(ConfigurationFile file) {
        Map<ConfigurationNode, Object> map = new HashMap<>();
        map.put(this.configBurnerBlock, this.useBurner);
        map.put(this.configSpecialBlockType, this.specialBlock.getId());
        map.put(this.configSpecialBlockPercent, this.specialBlockPercent);
        return map;
    }

    @Override
    public AbstractShipsVessel deserializeExtra(ConfigurationFile file) {
        this.useBurner = file.parseBoolean(this.configBurnerBlock).get();
        this.specialBlockPercent = file.parseDouble(this.configSpecialBlockPercent).get().floatValue();
        Optional<BlockType> type = file.parse(this.configSpecialBlockType, Parser.STRING_TO_BLOCK_TYPE);
        if(type.isPresent()){
            this.specialBlock = type.get();
        }else{
            System.err.println("A error occurred. " + file.parseString(this.configSpecialBlockType).get() + " is not a BlockType supported.");
        }
        return this;
    }

    @Override
    public ShipType getType() {
        return ShipType.AIRSHIP;
    }
}
