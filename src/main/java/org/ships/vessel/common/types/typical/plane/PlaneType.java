package org.ships.vessel.common.types.typical.plane;

import org.core.TranslateCore;
import org.core.config.ConfigurationStream;
import org.core.inventory.item.ItemTypes;
import org.core.permission.CorePermission;
import org.core.platform.plugin.Plugin;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.FuelledShipType;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.io.File;
import java.util.Collections;

public class PlaneType extends AbstractShipType<Plane> implements FuelledShipType<Plane> {

    public PlaneType() {
        this("Plane", new File(ShipsPlugin.getPlugin().getConfigFolder(), "/Configuration/ShipType/Plane." + TranslateCore.getPlatform().getConfigFormat().getFileType()[0]));
    }

    public PlaneType(String name, File file) {
        this(ShipsPlugin.getPlugin(), name, TranslateCore.createConfigurationFile(file, TranslateCore.getPlatform().getConfigFormat()), BlockTypes.AIR);
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

    @Override
    public @NotNull CorePermission getMoveOwnPermission() {
        return Permissions.PLANE_MOVE_OWN;
    }

    @Override
    public @NotNull CorePermission getMoveOtherPermission() {
        return Permissions.PLANE_MOVE_OTHER;
    }

    @Override
    public @NotNull CorePermission getMakePermission() {
        return Permissions.PLANE_MAKE;
    }
}
