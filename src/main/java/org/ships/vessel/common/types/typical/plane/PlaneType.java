package org.ships.vessel.common.types.typical.plane;

import org.core.CorePlugin;
import org.core.config.ConfigurationStream;
import org.core.inventory.item.ItemTypes;
import org.core.platform.Plugin;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.FuelledShipType;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.io.File;
import java.util.Collections;

public class PlaneType extends AbstractShipType<Plane> implements FuelledShipType<Plane> {

    public PlaneType() {
        this("Plane", new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/ShipType/Plane." + CorePlugin.getPlatform().getConfigFormat().getFileType()[0]));
    }

    public PlaneType(String name, File file) {
        this(ShipsPlugin.getPlugin(), name, CorePlugin.createConfigurationFile(file, CorePlugin.getPlatform().getConfigFormat()), BlockTypes.AIR);
    }

    public PlaneType(Plugin plugin, String displayName, ConfigurationStream.ConfigurationFile file, BlockType... types) {
        super(plugin, displayName, file, types);
    }

    @Override
    protected void createDefault(ConfigurationStream.@NotNull ConfigurationFile file) {
        this.file.set(MAX_SPEED, 20);
        this.file.set(FUEL_CONSUMPTION, 1);
        this.file.set(FUEL_SLOT, "Bottom");
        this.file.set(ALTITUDE_SPEED, 5);
        this.file.set(FUEL_TYPES, Collections.singleton(ItemTypes.COAL_BLOCK.get()));
    }

    @Override
    public @NotNull Plane createNewVessel(@NotNull SignTileEntity ste, @NotNull SyncBlockPosition bPos) {
        return new Plane(ste, bPos, this);
    }
}
