package org.ships.vessel.common.types.typical.opship;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.core.platform.Plugin;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Deprecated
public class OPShipType implements ShipType {

    public static class Default extends OPShipType {

        public Default(){
            super(new File(ShipsPlugin.getPlugin().getShipsConigFolder(),
                            "/Configuration/ShipType/ships.opship.temp"),
                    "OPShip");
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
    protected Set<VesselFlag<?>> flags = new HashSet<>();

    protected final String[] MAX_SPEED = {"Speed", "Max"};
    protected final String[] ALTITUDE_SPEED = {"Speed", "Altitude"};

    public OPShipType(File file, String display){
        this(CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.YAML), display);
    }

    public OPShipType(ConfigurationFile file, String display){
        this.file = file;
        this.blockList = new ExpandedBlockList(getFile(), ShipsPlugin.getPlugin().getBlockList());
        this.display = display; }

    @Override
    public String getDisplayName() {
        return this.display;
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

    @Override
    public ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public Vessel createNewVessel(SignTileEntity ste, BlockPosition bPos) {
        return new OPShip(ste, bPos, this);
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
}
