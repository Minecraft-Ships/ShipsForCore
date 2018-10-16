package org.ships.vessel.common.types;

import org.core.entity.living.human.player.User;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.result.FailedMovement;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.vessel.structure.AbstractPosititionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Map;
import java.util.Optional;

public abstract class AbstractShipsVessel implements ShipsVessel {

    protected PositionableShipsStructure positionableShipsStructure;
    protected Map<User, CrewPermission> crewsPermission;
    protected CrewPermission defaultPermission;
    protected ExpandedBlockList blockList;

    public AbstractShipsVessel(LiveSignTileEntity licence){
        this.positionableShipsStructure = new AbstractPosititionableShipsStructure(licence.getPosition());
    }

    public abstract Optional<FailedMovement> meetsRequirement(MovingBlockSet movingBlocks);

    @Override
    public ExpandedBlockList getBlockList() {
        return this.blockList;
    }

    @Override
    public PositionableShipsStructure getStructure() {
        return this.positionableShipsStructure;
    }

    @Override
    public Map<User, CrewPermission> getCrew() {
        return this.crewsPermission;
    }

    @Override
    public CrewPermission getDefaultPermission() {
        return this.defaultPermission;
    }
}
