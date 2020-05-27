package org.ships.vessel.common.types.typical.marsship;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlock;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.movement.result.data.RequiredPercentMovementData;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.*;

public class Marsship extends AbstractShipsVessel implements AirType, org.ships.vessel.common.assits.VesselRequirement {

    protected float specialBlockPercent = ShipType.MARSSHIP.getDefaultSpecialBlockPercent();
    protected Set<BlockType> specialBlocks = ShipType.MARSSHIP.getDefaultSpecialBlockType();

    protected ConfigurationNode configSpecialBlockPercent = new ConfigurationNode("Block", "Special", "Percent");
    protected ConfigurationNode configSpecialBlockType = new ConfigurationNode("Block", "Special", "Type");


    public Marsship(MarsshipType type, LiveSignTileEntity licence) {
        super(licence, type);
    }

    public Marsship(MarsshipType type, SignTileEntity ste, SyncBlockPosition position) {
        super(ste, position, type);
    }

    public float getSpecialBlockPercent(){
        return this.specialBlockPercent;
    }

    public Set<BlockType> getSpecialBlocks(){
        return this.specialBlocks;
    }

    @Override
    public Map<String, String> getExtraInformation() {
        Map<String, String> map = new HashMap<>();
        map.put("Special Block", CorePlugin.toString("", Parser.STRING_TO_BLOCK_TYPE::unparse, this.specialBlocks));
        map.put("Required Percent", this.specialBlockPercent + "");
        return map;
    }

    @Override
    public Map<ConfigurationNode, Object> serialize(ConfigurationFile file) {
        Map<ConfigurationNode, Object> map = new HashMap<>();
        map.put(this.configSpecialBlockType, Parser.unparseList(Parser.STRING_TO_BLOCK_TYPE, this.specialBlocks));
        map.put(this.configSpecialBlockPercent, this.specialBlockPercent);
        return map;
    }

    @Override
    public AbstractShipsVessel deserializeExtra(ConfigurationFile file) {
        this.specialBlockPercent = file.parseDouble(this.configSpecialBlockPercent).get().floatValue();
        Optional<List<BlockType>> opSpecialBlocks = file.parseList(this.configSpecialBlockType, Parser.STRING_TO_BLOCK_TYPE);
        this.specialBlocks = opSpecialBlocks.<Set<BlockType>>map(HashSet::new).orElseGet(HashSet::new);
        return this;
    }

    @Override
    public void meetsRequirements(MovementContext context) throws MoveException {
        if(!context.isStrictMovement()){
            return;
        }
        int specialBlocks = 0;
        for (MovingBlock block : context.getMovingStructure()){
            BlockDetails details = block.getStoredBlockData();
            if(this.specialBlocks.stream().anyMatch(b -> b.equals(details.getType()))){
                specialBlocks++;
            }
        }
        float specialBlockPercent = ((specialBlocks * 100.0f)/context.getMovingStructure().stream().filter(m -> !m.getStoredBlockData().getType().equals(BlockTypes.AIR.get())).count());
        if((this.specialBlockPercent != 0) && specialBlockPercent <= this.specialBlockPercent){
            throw new MoveException(new AbstractFailedMovement(this, MovementResult.NOT_ENOUGH_PERCENT, new RequiredPercentMovementData(this.specialBlocks.iterator().next(), this.specialBlockPercent, specialBlockPercent)));
        }
    }

    @Override
    public void processRequirements(MovementContext context) throws MoveException {
    }
}
