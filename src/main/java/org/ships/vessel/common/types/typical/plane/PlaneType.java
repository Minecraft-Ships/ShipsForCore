package org.ships.vessel.common.types.typical.plane;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.core.inventory.item.ItemType;
import org.core.inventory.item.ItemTypes;
import org.core.inventory.item.type.post.ItemTypes1V13;
import org.core.platform.Plugin;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.ShipType;

import java.io.File;
import java.util.*;

public class PlaneType implements ShipType<Plane> {

    private ConfigurationFile file;
    private ExpandedBlockList blockList;
    private String name;
    private Set<VesselFlag<?>> flags = new HashSet<>();

    private final String[] MAX_SPEED = {"Speed", "Max"};
    private final String[] ALTITUDE_SPEED = {"Speed", "Altitude"};
    private final String[] FUEL_CONSUMPTION = {"Block", "Fuel", "Consumption"};
    private final String[] FUEL_SLOT = {"Block", "Fuel", "Slot"};
    private final String[] FUEL_TYPES = {"Block", "Fuel", "Types"};

    public PlaneType(){
        this("Plane", new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/ShipType/Plane.temp"));
    }

    public PlaneType(String name, File file){
        this.name = name;
        this.file = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.DEFAULT);
        if(!this.file.getFile().exists()){
            this.file.set(new ConfigurationNode(this.MAX_SPEED), 20);
            this.file.set(new ConfigurationNode(this.FUEL_CONSUMPTION), 1);
            this.file.set(new ConfigurationNode(this.FUEL_SLOT), "Bottom");
            this.file.set(new ConfigurationNode(this.ALTITUDE_SPEED), 5);
            this.file.set(new ConfigurationNode(this.FUEL_TYPES), new ArrayList<>(Collections.singletonList(ItemTypes.COAL_BLOCK.getId())));
            this.file.save();
        }
        this.blockList = new ExpandedBlockList(getFile(), ShipsPlugin.getPlugin().getBlockList());
    }

    public int getDefaultFuelConsumption(){
        return this.file.parseInt(new ConfigurationNode(this.FUEL_CONSUMPTION)).get();
    }

    public boolean isUsingTopSlot(){
        String slot = this.file.parseString(new ConfigurationNode(this.FUEL_SLOT)).get();
        return "top".equals(slot.toLowerCase());
    }

    public Set<ItemType> getDefaultFuelTypes(){
        return new HashSet<>(this.file.parseList(new ConfigurationNode(this.FUEL_TYPES), Parser.STRING_TO_ITEM_TYPE).get());
    }

    @Override
    public String getDisplayName() {
        return this.name;
    }

    @Override
    public Plugin getPlugin() {
        return ShipsPlugin.getPlugin();
    }

    @Override
    public ExpandedBlockList getDefaultBlockList() {
        return this.blockList;
    }

    @Override
    public int getDefaultMaxSpeed() {
        return this.file.parse(new ConfigurationNode(this.MAX_SPEED), Parser.STRING_TO_INTEGER).get();
    }

    @Override
    public int getDefaultAltitudeSpeed() {
        return this.file.parse(new ConfigurationNode(this.ALTITUDE_SPEED), Parser.STRING_TO_INTEGER).get();
    }

    @Override
    public ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public Plane createNewVessel(SignTileEntity ste, BlockPosition bPos) {
        return new Plane(ste, bPos, this);
    }

    @Override
    public BlockType[] getIgnoredTypes() {
        return new BlockType[]{BlockTypes.AIR.get()};
    }

    @Override
    public Set<VesselFlag<?>> getFlags() {
        return this.flags;
    }

    @Override
    public String getName() {
        return this.getDisplayName();
    }
}
