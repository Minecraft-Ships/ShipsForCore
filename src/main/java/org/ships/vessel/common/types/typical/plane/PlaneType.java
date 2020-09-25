package org.ships.vessel.common.types.typical.plane;

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
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.ShipType;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PlaneType implements ShipType<Plane> {

    private final ConfigurationStream.ConfigurationFile file;
    private final ExpandedBlockList blockList;
    private final String name;
    private final Set<VesselFlag<?>> flags = new HashSet<>();

    private final ConfigurationNode.KnownParser.SingleKnown<Integer> MAX_SPEED = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Speed", "Max");
    private final ConfigurationNode.KnownParser.SingleKnown<Integer> ALTITUDE_SPEED = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Speed", "Altitude");
    private final ConfigurationNode.KnownParser.SingleKnown<Integer> FUEL_CONSUMPTION = new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Block", "Fuel", "Consumption");
    private final ConfigurationNode.KnownParser.SingleKnown<FuelSlot> FUEL_SLOT = new ConfigurationNode.KnownParser.SingleKnown<>(new StringToEnumParser<>(FuelSlot.class),"Block", "Fuel", "Slot");
    private final ConfigurationNode.KnownParser.CollectionKnown<ItemType, Set<ItemType>> FUEL_TYPES = new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_ITEM_TYPE, "Block", "Fuel", "Types");

    public PlaneType(){
        this("Plane", new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/ShipType/Plane." + CorePlugin.getPlatform().getConfigFormat().getFileType()[0]));
    }

    public PlaneType(String name, File file){
        this.name = name;
        this.file = CorePlugin.createConfigurationFile(file, CorePlugin.getPlatform().getConfigFormat());
        if(!this.file.getFile().exists()){
            this.file.set(this.MAX_SPEED, 20);
            this.file.set(this.FUEL_CONSUMPTION, 1);
            this.file.set(this.FUEL_SLOT, "Bottom");
            this.file.set(this.ALTITUDE_SPEED, 5);
            this.file.set(this.FUEL_TYPES, Collections.singleton(ItemTypes.COAL_BLOCK));
            this.file.save();
        }
        this.blockList = new ExpandedBlockList(getFile(), ShipsPlugin.getPlugin().getBlockList());
    }

    public int getDefaultFuelConsumption(){
        return this.file.getInteger(this.FUEL_CONSUMPTION).get();
    }

    public boolean isUsingTopSlot(){
        String slot = this.file.getString(this.FUEL_SLOT).get();
        return "top".equals(slot.toLowerCase());
    }

    public Set<ItemType> getDefaultFuelTypes(){
        return this.file.parseCollection(this.FUEL_TYPES, new HashSet<>());
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

    @Override
    public ConfigurationStream.ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public Plane createNewVessel(SignTileEntity ste, SyncBlockPosition bPos) {
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
