package org.ships.vessel.common.types.opship;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.util.regex.Pattern;

public class OPShipType <V extends OPShip> implements CloneableShipType<V> {

    public static class Default extends OPShipType<OPShip>{

        public Default(){
            super(new File(ShipsPlugin.getPlugin().getShipsConigFolder(),
                            "/Configuration/ShipType/ships.opship.temp"),
                    "OPShip", OPShip.class);
            if(!this.file.getFile().exists()){
                this.file.set(new ConfigurationNode(this.MAX_SPEED), 10);
                this.file.set(new ConfigurationNode(this.ALTITUDE_SPEED), 5);
                this.file.save();
            }
        }
    }

    protected ConfigurationFile file;
    protected ExpandedBlockList blockList;
    protected String display;
    protected Class<V> vesselClass;

    protected final String[] MAX_SPEED = {"Speed", "Max"};
    protected final String[] ALTITUDE_SPEED = {"Speed", "Altitude"};

    public OPShipType(File file, String display, Class<V> vesselClass){
        this(CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.YAML), display, vesselClass);
    }

    public OPShipType(ConfigurationFile file, String display, Class<V> vesselClass){
        this.file = file;
        this.blockList = new ExpandedBlockList(getFile(), ShipsPlugin.getPlugin().getBlockList());
        this.display = display;
        this.vesselClass = vesselClass;
    }

    @Override
    public String getDisplayName() {
        return this.display;
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

    @Override
    public ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public <T extends Vessel> CloneableShipType<T> clone(File root, String display, Class<T> vesselClass) {
        if(vesselClass.isAssignableFrom(OPShip.class)){
            Class<? extends OPShip> vesselClass2 = (Class<? extends OPShip>) vesselClass;
            return (CloneableShipType<T>) new OPShipType<>(root, display, vesselClass2);
        }
        return null;
    }

    @Override
    public Class<V> getVesselClass() {
        return this.vesselClass;
    }

    @Override
    public BlockType[] getIgnoredTypes() {
        return new BlockType[]{BlockTypes.AIR};
    }

    @Override
    public String getId() {
        String id = this.file.getFile().getName();
        String[] split = id.split(Pattern.quote("."));
        split[split.length - 1] = "";
        id = CorePlugin.toString(".", t -> t, split);
        id = id.substring(0, id.length() - 1);
        return id.replace(".", ":");
    }

    @Override
    public String getName() {
        return getDisplayName();
    }
}
