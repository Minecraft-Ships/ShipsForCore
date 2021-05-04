package org.ships.vessel.common.types.typical.watership;

import org.array.utils.ArrayUtils;
import org.core.CorePlugin;
import org.core.config.ConfigurationStream;
import org.core.platform.Plugin;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.grouptype.versions.BlockGroups1V13;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.assits.shiptype.SpecialBlockShipType;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.io.File;

public class WaterShipType extends AbstractShipType<WaterShip> implements CloneableShipType<WaterShip>, SpecialBlockShipType<WaterShip> {

    public WaterShipType() {
        this("Ship", new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/ShipType/Watership." + CorePlugin.getPlatform().getConfigFormat().getFileType()[0]));
    }

    public WaterShipType(String name, File file) {
        this(ShipsPlugin.getPlugin(), name, CorePlugin.createConfigurationFile(file, CorePlugin.getPlatform().getConfigFormat()), BlockTypes.AIR, BlockTypes.WATER);
    }

    public WaterShipType(Plugin plugin, String displayName, ConfigurationStream.ConfigurationFile file, BlockType... types) {
        super(plugin, displayName, file, types);
    }

    @Override
    public WaterShip createNewVessel(SignTileEntity ste, SyncBlockPosition bPos) {
        return new WaterShip(this, ste, bPos);
    }

    @Override
    protected void createDefault(ConfigurationStream.ConfigurationFile file) {
        this.file.set(MAX_SPEED, 10);
        this.file.set(ALTITUDE_SPEED, 5);
        this.file.set(SPECIAL_BLOCK_PERCENT, 25);
        this.file.set(SPECIAL_BLOCK_TYPE, ArrayUtils.ofSet(BlockGroups1V13.WOOL.getGrouped()));
    }

    @Override
    public CloneableShipType<WaterShip> cloneWithName(File file, String name) {
        return new WaterShipType(name, file);
    }

    @Override
    public CloneableShipType<WaterShip> getOriginType() {
        return ShipType.WATERSHIP;
    }
}
