package org.ships.vessel.common.types.typical.submarine;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.core.inventory.item.ItemType;
import org.core.inventory.item.ItemTypes;
import org.core.platform.Plugin;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SubmarineType implements ShipType {

    protected ConfigurationFile file;
    protected ExpandedBlockList blockList;

    private final String[] MAX_SPEED = {"Speed", "Max"};
    private final String[] ALTITUDE_SPEED = {"Speed", "Altitude"};
    private final String[] SPECIAL_BLOCK_TYPE = {"Block", "Special", "Type"};
    private final String[] SPECIAL_BLOCK_PERCENT = {"Block", "Special", "Percent"};
    private final String[] FUEL_CONSUMPTION = {"Block", "Fuel", "Consumption"};
    private final String[] FUEL_SLOT = {"Block", "Fuel", "Slot"};
    private final String[] FUEL_TYPES = {"Block", "Fuel", "Types"};

    public SubmarineType(){
        File file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/ShipType/" + getId().replaceAll(":", ".") + ".temp");
        this.file = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.DEFAULT);
        if((!this.file.getFile().exists()) || (!this.file.parseDouble(new ConfigurationNode(this.SPECIAL_BLOCK_PERCENT)).isPresent())){
            this.file.set(new ConfigurationNode(this.SPECIAL_BLOCK_PERCENT), 75.0f);
            this.file.set(new ConfigurationNode(this.SPECIAL_BLOCK_TYPE), Parser.unparseList(Parser.STRING_TO_BLOCK_TYPE, Arrays.asList(BlockTypes.IRON_BLOCK.get())));
            this.file.set(new ConfigurationNode(this.FUEL_CONSUMPTION), 1);
            this.file.set(new ConfigurationNode(this.FUEL_SLOT), "Bottom");
            this.file.set(new ConfigurationNode(this.FUEL_TYPES), new ArrayList<>(Arrays.asList(ItemTypes.COAL_BLOCK.getId())));
            this.file.set(new ConfigurationNode(this.MAX_SPEED), 10);
            this.file.set(new ConfigurationNode(this.ALTITUDE_SPEED), 5);
            this.file.save();
        }
        this.blockList = new ExpandedBlockList(getFile(), ShipsPlugin.getPlugin().getBlockList());
    }

    public float getDefaultSpecialBlockPercent(){
        return this.file.parseDouble(new ConfigurationNode(this.SPECIAL_BLOCK_PERCENT)).get().floatValue();
    }

    public Set<BlockType> getDefaultSpecialBlockType(){
        return new HashSet<>(this.file.parseList(new ConfigurationNode(this.SPECIAL_BLOCK_TYPE), Parser.STRING_TO_BLOCK_TYPE).get());
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
        return "Submarine";
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
    public Vessel createNewVessel(SignTileEntity ste, BlockPosition bPos) {
        return new Submarine(ste, bPos);
    }

    @Override
    public BlockType[] getIgnoredTypes() {
        return new BlockType[]{BlockTypes.AIR.get(), BlockTypes.WATER.get()};
    }

    @Override
    public String getName() {
        return getDisplayName();
    }
}
