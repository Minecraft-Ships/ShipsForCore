package org.ships.vessel.common.types.typical.submarine;

import org.array.utils.ArrayUtils;
import org.core.CorePlugin;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.config.parser.parsers.StringToEnumParser;
import org.core.inventory.item.ItemType;
import org.core.inventory.item.ItemTypes;
import org.core.platform.Plugin;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.assits.shiptype.SerializableShipType;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.ShipType;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SubmarineType implements CloneableShipType<Submarine>, SerializableShipType<Submarine> {

    protected ConfigurationStream.ConfigurationFile file;
    protected ExpandedBlockList blockList;
    protected String name;
    protected Set<VesselFlag<?>> flags = new HashSet<>();

    private final ConfigurationNode.KnownParser.SingleKnown<Integer> MAX_SPEED = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Speed", "Max");
    private final ConfigurationNode.KnownParser.SingleKnown<Integer> ALTITUDE_SPEED = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Speed", "Altitude");
    private final ConfigurationNode.KnownParser.CollectionKnown<BlockType, Set<BlockType>> SPECIAL_BLOCK_TYPE = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_BLOCK_TYPE, "Block", "Special", "Type");
    private final ConfigurationNode.KnownParser.SingleKnown<Double> SPECIAL_BLOCK_PERCENT = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_DOUBLE, "Block", "Special", "Percent");
    private final ConfigurationNode.KnownParser.SingleKnown<Integer> FUEL_CONSUMPTION = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Block", "Fuel", "Consumption");
    private final ConfigurationNode.KnownParser.SingleKnown<FuelSlot> FUEL_SLOT = new ConfigurationNode.KnownParser.SingleKnown<>(new StringToEnumParser<>(FuelSlot.class), "Block", "Fuel", "Slot");
    private final ConfigurationNode.KnownParser.CollectionKnown<ItemType, Set<ItemType>> FUEL_TYPES = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_ITEM_TYPE, "Block", "Fuel", "Types");

    public SubmarineType(){
        this("Submarine", new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/ShipType/Submarine." + CorePlugin.getPlatform().getConfigFormat().getFileType()[0]));
    }

    public SubmarineType(String name, File file){
        this.name = name;
        this.file = CorePlugin.createConfigurationFile(file, CorePlugin.getPlatform().getConfigFormat());
        if((!this.file.getFile().exists()) || (!this.file.getDouble(this.SPECIAL_BLOCK_PERCENT).isPresent())){
            this.file.set(this.SPECIAL_BLOCK_PERCENT, 75.0);
            this.file.set(this.SPECIAL_BLOCK_TYPE, Collections.singleton(BlockTypes.IRON_BLOCK.get()));
            this.file.set(this.FUEL_CONSUMPTION, 1);
            this.file.set(this.FUEL_SLOT, FuelSlot.BOTTOM);
            this.file.set(this.FUEL_TYPES, Collections.singleton(ItemTypes.COAL_BLOCK));
            this.file.set(this.MAX_SPEED, 10);
            this.file.set(this.ALTITUDE_SPEED, 5);
            this.file.save();
        }
        this.blockList = new ExpandedBlockList(getFile(), ShipsPlugin.getPlugin().getBlockList());
    }

    public float getDefaultSpecialBlockPercent(){
        return this.file.getDouble(this.SPECIAL_BLOCK_PERCENT).get().floatValue();
    }

    public Set<BlockType> getDefaultSpecialBlockType(){
        return this.file.parseCollection(this.SPECIAL_BLOCK_TYPE, new HashSet<>());
    }

    public int getDefaultFuelConsumption(){
        return this.file.getInteger(this.FUEL_CONSUMPTION).get();
    }

    public boolean isUsingTopSlot(){
        return this.file.parse(this.FUEL_SLOT).get().equals(FuelSlot.TOP);
    }

    public Set<ItemType> getDefaultFuelTypes(){
        return this.file.parseCollection(this.FUEL_TYPES, new HashSet<>());
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
        return this.file.parse(this.MAX_SPEED, Parser.STRING_TO_INTEGER).get();
    }

    @Override
    public int getDefaultAltitudeSpeed() {
        return this.file.parse(this.ALTITUDE_SPEED, Parser.STRING_TO_INTEGER).get();
    }

    @Override
    public ConfigurationStream.ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public Submarine createNewVessel(SignTileEntity ste, SyncBlockPosition bPos) {
        return new Submarine(this, ste, bPos);
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

    public SubmarineType cloneWithName(File file, String name) {
        return new SubmarineType(name, file);
    }

    public SubmarineType getOriginType() {
        return ShipType.SUBMARINE;
    }

    @Override
    public void setMaxSpeed(int speed) {
        this.file.set(this.MAX_SPEED, speed);
    }

    @Override
    public void setAltitudeSpeed(int speed) {
        this.file.set(this.ALTITUDE_SPEED, speed);
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
