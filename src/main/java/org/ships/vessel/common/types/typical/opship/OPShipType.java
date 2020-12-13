package org.ships.vessel.common.types.typical.opship;

import org.core.CorePlugin;
import org.core.config.ConfigurationStream;
import org.core.platform.Plugin;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.io.File;

@Deprecated
public class OPShipType extends AbstractShipType<OPShip> {

    public OPShipType(){
        this("OPShip", new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/ShipType/OPShip." + CorePlugin.getPlatform().getConfigFormat().getFileType()[0]));
    }

    public OPShipType(String name, File file){
        this(ShipsPlugin.getPlugin(), name, CorePlugin.createConfigurationFile(file, CorePlugin.getPlatform().getConfigFormat()), BlockTypes.AIR.get());
    }

    public OPShipType(Plugin plugin, String displayName, ConfigurationStream.ConfigurationFile file, BlockType... types) {
        super(plugin, displayName, file, types);
    }

    @Override
    protected void createDefault(ConfigurationStream.ConfigurationFile file) {
        this.file.set(MAX_SPEED, 10);
        this.file.set(ALTITUDE_SPEED, 5);
    }

    @Override
    public OPShip createNewVessel(SignTileEntity ste, SyncBlockPosition bPos) {
        return new OPShip(ste, bPos, this);
    }
}
