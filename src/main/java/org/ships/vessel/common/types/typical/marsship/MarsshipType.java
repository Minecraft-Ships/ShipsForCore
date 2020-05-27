package org.ships.vessel.common.types.typical.marsship;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.core.platform.Plugin;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.assits.shiptype.SerializableShipType;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.ShipType;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MarsshipType implements CloneableShipType<Marsship>, SerializableShipType<Marsship> {

    protected ConfigurationFile file;
    protected ExpandedBlockList blockList;
    protected String name;
    protected Set<VesselFlag<?>> flags = new HashSet<>();

    private final String[] MAX_SPEED = {"Speed", "Max"};
    private final String[] ALTITUDE_SPEED = {"Speed", "Altitude"};
    private final String[] SPECIAL_BLOCK_TYPE = {"Special", "Block", "Type"};
    private final String[] SPECIAL_BLOCK_PERCENT = {"Special", "Block", "Percent"};

    public MarsshipType(){
        this("Marsship", new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/ShipType/Marsship.temp"));
    }

    public MarsshipType(String name, File file){
        this.name = name;
        this.file = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.DEFAULT);
        if(!this.file.getFile().exists()){
            this.file.set(new ConfigurationNode(this.MAX_SPEED), 10);
            this.file.set(new ConfigurationNode(this.ALTITUDE_SPEED), 5);
            this.file.set(new ConfigurationNode(this.SPECIAL_BLOCK_PERCENT), 15);
            this.file.set(new ConfigurationNode(this.SPECIAL_BLOCK_TYPE), Parser.unparseList(Parser.STRING_TO_BLOCK_TYPE, Arrays.asList(BlockTypes.DAYLIGHT_DETECTOR.get())));
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
    public Marsship createNewVessel(SignTileEntity ste, SyncBlockPosition bPos) {
        return new Marsship(this, ste, bPos);
    }

    @Override
    public BlockType[] getIgnoredTypes() {
        return new BlockType[]{BlockTypes.AIR.get(), BlockTypes.WATER.get()};
    }

    @Override
    public Set<VesselFlag<?>> getFlags() {
        return this.flags;
    }

    @Override
    public String getName() {
        return getDisplayName();
    }

    public CloneableShipType cloneWithName(File file, String name) {
        return new MarsshipType(name, file);
    }

    public CloneableShipType getOriginType() {
        return ShipType.MARSSHIP;
    }

    @Override
    public void setMaxSpeed(int speed) {
        this.file.set(new ConfigurationNode(MAX_SPEED), speed);
    }

    @Override
    public void setAltitudeSpeed(int speed) {
        this.file.set(new ConfigurationNode(ALTITUDE_SPEED), speed);
    }

    @Override
    public void register(VesselFlag<?> flag) {
        this.flags.add(flag);
    }

    @Override
    public void save() {
        this.getFlags().stream().forEach(f -> setFlag(f));
        this.getFile().save();
    }

    private <F extends Object> void setFlag(VesselFlag<F> f){
        if(!(f instanceof VesselFlag.Serializable)){
            return;
        }
        VesselFlag.Serializable sFlag = (VesselFlag.Serializable)f;
        String trueId = sFlag.getId().split(":")[1];
        String[] flagId = trueId.split(".");
        if(flagId.length == 0){
            flagId = new String[]{trueId};
        }
        F value = f.getValue().orElse(null);
        ConfigurationNode node = new ConfigurationNode(new ConfigurationNode("flag", sFlag.getId().split(":")[0]), flagId);
        this.getFile().set(node, f.getParser(), value);
    }
}
