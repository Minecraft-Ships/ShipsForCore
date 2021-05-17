package org.ships.vessel.common.types.typical.marsship;

import org.core.CorePlugin;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.platform.Plugin;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.assits.shiptype.SpecialBlockShipType;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.io.File;
import java.util.Collections;

public class MarsshipType extends AbstractShipType<Marsship> implements CloneableShipType<Marsship>, SpecialBlockShipType<Marsship> {

    public MarsshipType() {
        this("Marsship", new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/ShipType/MarsShip." + CorePlugin.getPlatform().getConfigFormat().getFileType()[0]));
    }

    public MarsshipType(String name, File file) {
        this(ShipsPlugin.getPlugin(), name, CorePlugin.createConfigurationFile(file, CorePlugin.getPlatform().getConfigFormat()), BlockTypes.AIR, BlockTypes.WATER);
    }

    public MarsshipType(Plugin plugin, String displayName, ConfigurationStream.ConfigurationFile file, BlockType... types) {
        super(plugin, displayName, file, types);
    }

    @Override
    public MarsshipType cloneWithName(File file, String name) {
        return new MarsshipType(name, file);
    }

    @Override
    public MarsshipType getOriginType() {
        return ShipType.MARSSHIP;
    }

    @Override
    protected void createDefault(ConfigurationStream.@NotNull ConfigurationFile file) {
        this.file.set(MAX_SPEED, 10);
        this.file.set(ALTITUDE_SPEED, 5);
        this.file.set(SPECIAL_BLOCK_PERCENT, 15);
        this.file.set(SPECIAL_BLOCK_TYPE, Parser.STRING_TO_BLOCK_TYPE, Collections.singletonList(BlockTypes.DAYLIGHT_DETECTOR));
    }

    @Override
    public @NotNull Marsship createNewVessel(@NotNull SignTileEntity ste, @NotNull SyncBlockPosition bPos) {
        return new Marsship(this, ste, bPos);
    }
}
