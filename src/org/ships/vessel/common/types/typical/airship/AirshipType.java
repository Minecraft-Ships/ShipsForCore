package org.ships.vessel.common.types.typical.airship;

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
import org.ships.vessel.common.assits.shiptype.ClassicShipType;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AirshipType implements ShipType, ClassicShipType, CloneableShipType {

    protected ConfigurationFile file;
    protected ExpandedBlockList blockList;
    protected String name;

    private final String[] MAX_SPEED = {"Speed", "Max"};
    private final String[] ALTITUDE_SPEED = {"Speed", "Altitude"};
    private final String[] BURNER_BLOCK = {"Block", "Burner"};
    private final String[] SPECIAL_BLOCK_TYPE = {"Block", "Special", "Type"};
    private final String[] SPECIAL_BLOCK_PERCENT = {"Block", "Special", "Percent"};
    private final String[] FUEL_CONSUMPTION = {"Block", "Fuel", "Consumption"};
    private final String[] FUEL_SLOT = {"Block", "Fuel", "Slot"};
    private final String[] FUEL_TYPES = {"Block", "Fuel", "Types"};

    private final String[] LEGACY_FUEL_CONSUMPTION = {"ShipsData", "Config", "Fuel", "Consumption"};
    private final String[] LEGACY_SPECIAL_BLOCK_PERCENT = {"ShipsData", "Config", "Block", "Percent"};
    private final String[] LEGACY_MAX_SPEED = {"ShipsData", "Config", "Spped", "Boost"};
    private final String[] LEGACY_OWNER = {"ShipsData", "Player", "Name"};

    public AirshipType(){
        this("Airship", new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/ShipType/Airship.temp"));
    }

    public AirshipType(String name, File file){
        this.name = name;
        this.file = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.DEFAULT);
        if(!this.file.getFile().exists()){
            this.file.set(new ConfigurationNode(this.BURNER_BLOCK), true);
            this.file.set(new ConfigurationNode(this.SPECIAL_BLOCK_PERCENT), 60.0f);
            this.file.set(new ConfigurationNode(this.SPECIAL_BLOCK_TYPE), Parser.unparseList(Parser.STRING_TO_BLOCK_TYPE, BlockTypes.WHITE_WOOL.get().getLike()));
            this.file.set(new ConfigurationNode(this.FUEL_CONSUMPTION), 1);
            this.file.set(new ConfigurationNode(this.FUEL_SLOT), "Bottom");
            this.file.set(new ConfigurationNode(this.FUEL_TYPES), new ArrayList<>(Arrays.asList(ItemTypes.COAL.getId(), ItemTypes.CHARCOAL.getId())));
            this.file.set(new ConfigurationNode(this.MAX_SPEED), 10);
            this.file.set(new ConfigurationNode(this.ALTITUDE_SPEED), 5);
            this.file.save();
        }
        this.blockList = new ExpandedBlockList(getFile(), ShipsPlugin.getPlugin().getBlockList());
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

    public float getDefaultSpecialBlockPercent(){
        return this.file.parseDouble(new ConfigurationNode(this.SPECIAL_BLOCK_PERCENT)).get().floatValue();
    }

    public Set<BlockType> getDefaultSpecialBlockType(){
        return new HashSet<>(this.file.parseList(new ConfigurationNode(this.SPECIAL_BLOCK_TYPE), Parser.STRING_TO_BLOCK_TYPE).get());
    }

    public boolean isUsingBurner(){
        return this.file.parse(new ConfigurationNode(this.BURNER_BLOCK), Parser.STRING_TO_BOOLEAN).get();
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
    public ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public Vessel createNewVessel(SignTileEntity ste, BlockPosition bPos) {
        return new Airship(this, ste, bPos);
    }

    @Override
    public BlockType[] getIgnoredTypes() {
        return new BlockType[]{BlockTypes.AIR.get()};
    }

    @Override
    public String getName() {
        return getDisplayName();
    }

    @Override
    public Vessel createClassicVessel(SignTileEntity ste, BlockPosition blockPosition) {
        File classicFile = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "VesselData/" + ste.getLine(2).get().toPlain() + ".yml");
        ConfigurationFile config = CorePlugin.createConfigurationFile(classicFile, ConfigurationLoaderTypes.YAML);

        return null;
    }

    @Override
    public AirshipType cloneWithName(File file, String name) {
        return new AirshipType(name, file);
    }

    @Override
    public CloneableShipType getOriginType() {
        return ShipType.AIRSHIP;
    }
}
