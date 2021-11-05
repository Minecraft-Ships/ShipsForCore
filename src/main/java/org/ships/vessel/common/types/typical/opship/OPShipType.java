package org.ships.vessel.common.types.typical.opship;

import org.core.TranslateCore;
import org.core.config.ConfigurationStream;
import org.core.permission.CorePermission;
import org.core.platform.plugin.Plugin;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.io.File;

@Deprecated
public class OPShipType extends AbstractShipType<OPShip> {

    public OPShipType() {
        this("OPShip", new File(ShipsPlugin.getPlugin().getConfigFolder(), "/Configuration/ShipType/OPShip." + TranslateCore.getPlatform().getConfigFormat().getFileType()[0]));
    }

    public OPShipType(String name, File file) {
        this(ShipsPlugin.getPlugin(), name, TranslateCore.createConfigurationFile(file, TranslateCore.getPlatform().getConfigFormat()), BlockTypes.AIR);
    }

    public OPShipType(Plugin plugin, String displayName, ConfigurationStream.ConfigurationFile file, BlockType... types) {
        super(plugin, displayName, file, types);
    }

    @Override
    protected void createDefault(ConfigurationStream.@NotNull ConfigurationFile file) {
        this.file.set(MAX_SPEED, 10);
        this.file.set(ALTITUDE_SPEED, 5);
    }

    @Override
    public @NotNull OPShip createNewVessel(@NotNull SignTileEntity ste, @NotNull SyncBlockPosition bPos) {
        return new OPShip(ste, bPos, this);
    }

    @Override
    public @NotNull CorePermission getMoveOwnPermission() {
        return Permissions.OPSHIP_MOVE_OWN;
    }

    @Override
    public @NotNull CorePermission getMoveOtherPermission() {
        return Permissions.OPSHIP_MOVE_OTHER;
    }

    @Override
    public @NotNull CorePermission getMakePermission() {
        return Permissions.OPSHIP_MAKE;
    }
}
