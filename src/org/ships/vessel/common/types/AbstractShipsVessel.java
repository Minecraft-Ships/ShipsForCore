package org.ships.vessel.common.types;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.core.entity.living.human.player.User;
import org.core.text.TextColours;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.result.FailedMovement;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsFileLoader;
import org.ships.vessel.structure.AbstractPosititionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractShipsVessel implements ShipsVessel {

    protected PositionableShipsStructure positionableShipsStructure;
    protected Map<User, CrewPermission> crewsPermission = new HashMap<>();
    protected CrewPermission defaultPermission;
    protected File file;
    protected ExpandedBlockList blockList;
    protected int maxSpeed = 10;
    protected int altitudeSpeed = 2;

    public AbstractShipsVessel(LiveSignTileEntity licence){
        this.positionableShipsStructure = new AbstractPosititionableShipsStructure(licence.getPosition());
        this.file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "VesselData/" + getType().getId() + "/" + getName() + ".temp");
        this.blockList = new ExpandedBlockList(CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.YAML), ShipsPlugin.getPlugin().getBlockList());
    }

    public AbstractShipsVessel(SignTileEntity ste, BlockPosition position){
        this.positionableShipsStructure = new AbstractPosititionableShipsStructure(position);
        this.file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "VesselData/" + ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(t -> t.getDisplayName().equals(TextColours.stripColours(ste.getLine(1)))) + "/" + TextColours.stripColours(ste.getLine(2)) + ".temp");
        this.blockList = new ExpandedBlockList(CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.YAML), ShipsPlugin.getPlugin().getBlockList());
    }

    public abstract Optional<FailedMovement> meetsRequirement(MovingBlockSet movingBlocks);
    public abstract Map<ConfigurationNode, String> serialize(ConfigurationFile file);
    public abstract AbstractShipsVessel deserializeExtra(ConfigurationFile file);

    @Override
    public File getFile(){
        return this.file;
    }

    @Override
    public void save(){
        ShipsFileLoader fl = new ShipsFileLoader(this.getFile());
        fl.save(this);
    }

    @Override
    public ExpandedBlockList getBlockList() {
        return this.blockList;
    }

    @Override
    public PositionableShipsStructure getStructure() {
        System.out.println("PositionableStructure: " + this.positionableShipsStructure);
        return this.positionableShipsStructure;
    }

    @Override
    public int getMaxSpeed() {
        return this.maxSpeed;
    }

    @Override
    public int getAltitudeSpeed() {
        return this.altitudeSpeed;
    }

    @Override
    public Vessel setMaxSpeed(int speed) {
        this.maxSpeed = speed;
        return this;
    }

    @Override
    public Vessel setAltitudeSpeed(int speed) {
        this.altitudeSpeed = speed;
        return this;
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
