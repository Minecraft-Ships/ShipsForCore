package org.ships.vessel.common.types.typical.submarine;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;

import java.io.File;

public class SubmarineType implements ShipType {

    protected ConfigurationFile file;
    protected ExpandedBlockList blockList;

    private final String[] MAX_SPEED = {"Speed", "Max"};
    private final String[] ALTITUDE_SPEED = {"Speed", "Altitude"};

    public SubmarineType(){
        File file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/ShipType/" + getId().replaceAll(":", ".") + ".temp");
        this.file = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.DEFAULT);
        if(!this.file.getFile().exists()){
            this.file.save();
        }
        this.blockList = new ExpandedBlockList(getFile(), ShipsPlugin.getPlugin().getBlockList());
    }

    @Override
    public String getDisplayName() {
        return "Submarine";
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
    public String getId() {
        return "ships:submarine";
    }

    @Override
    public String getName() {
        return getDisplayName();
    }
}
