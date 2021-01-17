package org.ships.vessel.common.types.typical.submarine;

import org.core.CorePlugin;
import org.core.config.ConfigurationStream;
import org.core.inventory.item.ItemTypes;
import org.core.platform.Plugin;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.shiptype.FuelledShipType;
import org.ships.vessel.common.assits.shiptype.SpecialBlockShipType;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.io.File;
import java.util.Collections;

public class SubmarineType extends AbstractShipType<Submarine> implements SpecialBlockShipType<Submarine>, FuelledShipType<Submarine> {

    public SubmarineType(){
        this("Submarine", new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/ShipType/Submarine." + CorePlugin.getPlatform().getConfigFormat().getFileType()[0]));
    }

    public SubmarineType(String name, File file){
        this(ShipsPlugin.getPlugin(), name, CorePlugin.createConfigurationFile(file, CorePlugin.getPlatform().getConfigFormat()), BlockTypes.AIR.get(), BlockTypes.WATER.get());
    }

    public SubmarineType(Plugin plugin, String displayName, ConfigurationStream.ConfigurationFile file, BlockType... types) {
        super(plugin, displayName, file, types);
    }

    @Override
    protected void createDefault(ConfigurationStream.ConfigurationFile file) {
        this.file.set(SPECIAL_BLOCK_PERCENT, 75.0);
        this.file.set(SPECIAL_BLOCK_TYPE, Collections.singleton(BlockTypes.IRON_BLOCK.get()));
        this.file.set(FUEL_CONSUMPTION, 1);
        this.file.set(FUEL_SLOT, FuelSlot.BOTTOM);
        this.file.set(FUEL_TYPES, Collections.singleton(ItemTypes.COAL_BLOCK));
        this.file.set(MAX_SPEED, 10);
        this.file.set(ALTITUDE_SPEED, 5);
    }

    @Override
    public Submarine createNewVessel(SignTileEntity ste, SyncBlockPosition bPos) {
        return new Submarine(this, ste, bPos);
    }
}
