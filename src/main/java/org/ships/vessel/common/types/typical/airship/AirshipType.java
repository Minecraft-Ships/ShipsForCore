package org.ships.vessel.common.types.typical.airship;

import org.array.utils.ArrayUtils;
import org.core.CorePlugin;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.config.parser.parsers.StringToEnumParser;
import org.core.inventory.item.ItemType;
import org.core.inventory.item.ItemTypes;
import org.core.inventory.item.type.post.ItemTypes1V13;
import org.core.platform.Plugin;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.blocktypes.legacy.BlockTypes1V12;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.grouptype.versions.BlockGroups1V13;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.assits.shiptype.SerializableShipType;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.ShipType;

import java.io.File;
import java.util.*;

public class AirshipType implements CloneableShipType<Airship>, SerializableShipType<Airship> {

    protected ConfigurationStream.ConfigurationFile file;
    protected ExpandedBlockList blockList;
    protected String name;
    protected Set<VesselFlag<?>> flags = new HashSet<>();

    private final ConfigurationNode.KnownParser.SingleKnown<Integer> MAX_SPEED = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Speed", "Max");
    private final ConfigurationNode.KnownParser.SingleKnown<Integer> ALTITUDE_SPEED = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Speed", "Altitude");
    private final ConfigurationNode.KnownParser.SingleKnown<Boolean> BURNER_BLOCK = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_BOOLEAN, "Block", "Burner");
    private final ConfigurationNode.KnownParser.CollectionKnown<BlockType, Set<BlockType>> SPECIAL_BLOCK_TYPE = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_BLOCK_TYPE, "Block", "Special", "Type");
    private final ConfigurationNode.KnownParser.SingleKnown<Double> SPECIAL_BLOCK_PERCENT = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_DOUBLE, "Block", "Special", "Percent");
    private final ConfigurationNode.KnownParser.SingleKnown<Integer> FUEL_CONSUMPTION = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Block", "Fuel", "Consumption");
    private final ConfigurationNode.KnownParser.SingleKnown<FuelSlot> FUEL_SLOT = new ConfigurationNode.KnownParser.SingleKnown<>(new StringToEnumParser<>(FuelSlot.class), "Block", "Fuel", "Slot");
    private final ConfigurationNode.KnownParser.CollectionKnown<ItemType, Set<ItemType>> FUEL_TYPES = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_ITEM_TYPE, "Block", "Fuel", "Types");

    public AirshipType(){
        this("Airship", new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/ShipType/Airship." + CorePlugin.getPlatform().getConfigFormat().getFileType()[0]));
    }

    public AirshipType(String name, File file){
        this.name = name;
        this.file = CorePlugin.createConfigurationFile(file, CorePlugin.getPlatform().getConfigFormat());
        int[] mcVersion = CorePlugin.getPlatform().getMinecraftVersion();
        if(!this.file.getFile().exists()){
            this.file.set(this.BURNER_BLOCK, true);
            this.file.set(this.SPECIAL_BLOCK_PERCENT, 60.0f);
            if(mcVersion[1] == 12) {
                this.file.set(this.SPECIAL_BLOCK_TYPE, Collections.singleton(BlockTypes1V12.WOOL.get()));
            }else{
                this.file.set(this.SPECIAL_BLOCK_TYPE, ArrayUtils.ofSet(BlockGroups1V13.WOOL.getGrouped()));
            }
            this.file.set(this.FUEL_CONSUMPTION, 1);
            this.file.set(this.FUEL_SLOT, FuelSlot.BOTTOM);
            if(mcVersion[1] == 12) {
                this.file.set(this.FUEL_TYPES, Collections.singleton(ItemTypes.COAL));
            }else{
                this.file.set(this.FUEL_TYPES, ArrayUtils.ofSet(ItemTypes.COAL, ItemTypes1V13.CHARCOAL));
            }
            this.file.set(this.MAX_SPEED, 10);
            this.file.set(this.ALTITUDE_SPEED, 5);
            this.file.save();
        }
        this.blockList = new ExpandedBlockList(this.getFile(), ShipsPlugin.getPlugin().getBlockList());
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
        return this.file.getInteger(this.MAX_SPEED).get();
    }

    @Override
    public int getDefaultAltitudeSpeed() {
        return this.file.getInteger(this.ALTITUDE_SPEED).get();
    }

    public float getDefaultSpecialBlockPercent(){
        return this.file.getDouble(this.SPECIAL_BLOCK_PERCENT).get().floatValue();
    }

    public Set<BlockType> getDefaultSpecialBlockType(){
        return this.file.parseCollection(this.SPECIAL_BLOCK_TYPE, new HashSet<>());
    }

    public boolean isUsingBurner(){
        return this.file.getBoolean(this.BURNER_BLOCK).get();
    }

    public int getDefaultFuelConsumption(){
        return this.file.getInteger(this.FUEL_CONSUMPTION).get();
    }

    public boolean isUsingTopSlot(){
        return this.file.parse(this.FUEL_SLOT).get() == FuelSlot.TOP;
    }

    public Set<ItemType> getDefaultFuelTypes(){
        return this.file.parseCollection(this.FUEL_TYPES, new HashSet<>());
    }

    @Override
    public ConfigurationStream.ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public Airship createNewVessel(SignTileEntity ste, SyncBlockPosition bPos) {
        return new Airship(this, ste, bPos);
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
        return getDisplayName();
    }

    @Override
    public AirshipType cloneWithName(File file, String name) {
        return new AirshipType(name, file);
    }

    @Override
    public AirshipType getOriginType() {
        return ShipType.AIRSHIP;
    }

    @Override
    public void setMaxSpeed(int speed) {
        this.file.set(MAX_SPEED, speed);
    }

    @Override
    public void setAltitudeSpeed(int speed) {
        this.file.set(ALTITUDE_SPEED, speed);
    }

    @Override
    public void register(VesselFlag<?> flag) {
        this.flags.add(flag);
    }

    @Override
    public void save() {
        this.getFlags().forEach(this::setFlag);
        this.getFile().save();
    }

    private <F> void setFlag(VesselFlag<F> f){
        if(!(f instanceof VesselFlag.Serializable)){
            return;
        }
        VesselFlag.Serializable<F> sFlag = (VesselFlag.Serializable<F>)f;
        String trueId = sFlag.getId().split(":")[1];
        String[] flagId = trueId.split("\\.");
        if(flagId.length == 0){
            flagId = new String[]{trueId};
        }
        F value = f.getValue().orElse(null);
        ConfigurationNode node = new ConfigurationNode(ArrayUtils.join(String.class, new String[]{"flag", sFlag.getId().split(":")[0]}, flagId));
        this.getFile().set(node, f.getParser(), value);
    }
}
