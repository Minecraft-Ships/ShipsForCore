package org.ships.vessel.common.types.typical.watership;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.core.platform.Plugin;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.blocktypes.legacy.BlockTypes1V12;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.grouptype.versions.BlockGroups1V13;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class WaterShipType implements CloneableShipType {

    protected ConfigurationFile file;
    protected ExpandedBlockList blockList;
    protected String name;

    private final String[] MAX_SPEED = {"Speed", "Max"};
    private final String[] ALTITUDE_SPEED = {"Speed", "Altitude"};
    private final String[] SPECIAL_BLOCK_TYPE = {"Special", "Block", "Type"};
    private final String[] SPECIAL_BLOCK_PERCENT = {"Special", "Block", "Percent"};

    public WaterShipType(){
        this("Ship", new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/ShipType/Watership.temp"));
    }

    public WaterShipType(String name, File file){
        int[] mcVersion = CorePlugin.getPlatform().getMinecraftVersion();
        this.name = name;
        this.file = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.DEFAULT);
        if(!this.file.getFile().exists()){
            this.file.set(new ConfigurationNode(this.MAX_SPEED), 10);
            this.file.set(new ConfigurationNode(this.ALTITUDE_SPEED), 5);
            this.file.set(new ConfigurationNode(this.SPECIAL_BLOCK_PERCENT), 25);
            if(mcVersion[1] == 12){
                this.file.set(new ConfigurationNode(this.SPECIAL_BLOCK_TYPE), Arrays.asList(Parser.STRING_TO_BLOCK_TYPE.unparse(BlockTypes1V12.WOOL.get())));
            }else {
                this.file.set(new ConfigurationNode(this.SPECIAL_BLOCK_TYPE), Parser.unparseList(Parser.STRING_TO_BLOCK_TYPE, Arrays.asList(BlockGroups1V13.WOOL.getGrouped())));
            }
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
        return file.parse(new ConfigurationNode(this.MAX_SPEED), Parser.STRING_TO_INTEGER).get();
    }

    @Override
    public int getDefaultAltitudeSpeed() {
        return file.parse(new ConfigurationNode(this.ALTITUDE_SPEED), Parser.STRING_TO_INTEGER).get();
    }

    public float getDefaultSpecialBlockPercent(){
        return this.file.parseDouble(new ConfigurationNode(this.SPECIAL_BLOCK_PERCENT)).get().floatValue();
    }

    public Set<BlockType> getDefaultSpecialBlockType(){
        return new HashSet<>(this.file.parseList(new ConfigurationNode(this.SPECIAL_BLOCK_TYPE), Parser.STRING_TO_BLOCK_TYPE).get());
    }

    @Override
    public ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public Vessel createNewVessel(SignTileEntity ste, BlockPosition bPos) {
        return new WaterShip(this, ste, bPos);
    }

    @Override
    public BlockType[] getIgnoredTypes() {
        return new BlockType[]{BlockTypes.AIR.get(), BlockTypes.WATER.get()};
    }

    @Override
    public String getName() {
        String name = this.file.getFile().getName();
        String[] split = name.split(Pattern.quote("."));
        int length = split.length;
        name = CorePlugin.toString(".", CorePlugin.strip(String.class, 0, length - 1, split));
        return name;
    }

    public CloneableShipType cloneWithName(File file, String name) {
        return new WaterShipType(name, file);
    }

    public CloneableShipType getOriginType() {
        return ShipType.WATERSHIP;
    }
}
