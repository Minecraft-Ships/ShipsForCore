package org.ships.vessel.common.types.typical.marsship;

import org.core.TranslateCore;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.permission.CorePermission;
import org.core.platform.plugin.Plugin;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.assits.shiptype.SpecialBlocksShipType;
import org.ships.vessel.common.requirement.MaxSizeRequirement;
import org.ships.vessel.common.requirement.MinSizeRequirement;
import org.ships.vessel.common.requirement.Requirement;
import org.ships.vessel.common.requirement.SpecialBlocksRequirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipTypes;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MarsshipType extends AbstractShipType<Marsship>
        implements CloneableShipType<Marsship>, SpecialBlocksShipType<Marsship> {

    public static final String NAME = "Marsship";

    private CorePermission moveOwnPermission = Permissions.MARSSHIP_MOVE_OWN;
    private CorePermission moveOtherPermission = Permissions.MARSSHIP_MOVE_OTHER;
    private CorePermission makePermission = Permissions.MARSSHIP_MAKE;

    private SpecialBlocksRequirement specialBlocksRequirement;
    private MinSizeRequirement minSizeRequirement;
    private MaxSizeRequirement maxSizeRequirement;

    private @Nullable Integer max;
    private int min;


    public MarsshipType() {
        this(NAME, new File(ShipsPlugin.getPlugin().getConfigFolder(),
                            "/Configuration/ShipType/MarsShip." + TranslateCore
                                    .getPlatform()
                                    .getConfigFormat()
                                    .getFileType()[0]));
    }

    public MarsshipType(String name, File file) {
        this(ShipsPlugin.getPlugin(), name,
             TranslateCore.getConfigManager().read(file, TranslateCore.getPlatform().getConfigFormat()), BlockTypes.AIR,
             BlockTypes.WATER);
    }

    public MarsshipType(Plugin plugin,
                        String displayName,
                        ConfigurationStream.ConfigurationFile file,
                        BlockType... types) {
        super(plugin, displayName, file, types);

        this.min = file.getInteger(MinSizeRequirement.MIN_SIZE, 0);
        file.getInteger(MaxSizeRequirement.MAX_SIZE).ifPresent(value -> this.max = value);

        if (!(plugin.equals(ShipsPlugin.getPlugin()) && displayName.equals(NAME))) {
            String pluginId = plugin.getPluginId();
            String name = displayName.toLowerCase().replace(" ", "");
            this.moveOwnPermission = TranslateCore
                    .getPlatform()
                    .register(new CorePermission(true, "ships", "move", "own", pluginId, name));
            this.moveOtherPermission = TranslateCore
                    .getPlatform()
                    .register(new CorePermission(false, "ships", "move", "other", pluginId, name));
            this.makePermission = TranslateCore
                    .getPlatform()
                    .register(new CorePermission(false, "ships", "make", pluginId, name));
        }
    }

    public MinSizeRequirement getMinimumRequirement() {
        return this.minSizeRequirement;
    }

    public MaxSizeRequirement getMaximumRequirement() {
        return this.maxSizeRequirement;
    }

    @Override
    public MarsshipType cloneWithName(File file, String name) {
        return new MarsshipType(name, file);
    }

    @Override
    public MarsshipType getOriginType() {
        return ShipTypes.MARSSHIP;
    }

    @Override
    protected void createDefault(ConfigurationStream.@NotNull ConfigurationFile file) {
        this.file.set(MAX_SPEED, 10);
        this.file.set(ALTITUDE_SPEED, 5);
        this.file.set(SPECIAL_BLOCK_PERCENT, 15);
        this.file.set(SPECIAL_BLOCK_TYPE, Parser.STRING_TO_BLOCK_TYPE,
                      Collections.singletonList(BlockTypes.DAYLIGHT_DETECTOR));
    }

    @Override
    public Collection<Requirement<?>> getDefaultRequirements() {
        if (this.specialBlocksRequirement == null) {
            this.specialBlocksRequirement = new SpecialBlocksRequirement(null, this.getDefaultSpecialBlocksPercent(),
                                                                         this.getDefaultSpecialBlockTypes());
        }

        if (this.maxSizeRequirement == null) {
            this.maxSizeRequirement = new MaxSizeRequirement(null, this.max);
        }
        if (this.minSizeRequirement == null) {
            this.minSizeRequirement = new MinSizeRequirement(null, this.min);
        }
        return List.of(this.maxSizeRequirement, this.minSizeRequirement, this.specialBlocksRequirement);
    }

    @Override
    public @NotNull Marsship createNewVessel(@NotNull SignSide side, @NotNull SyncBlockPosition bPos) {
        return new Marsship(side, bPos, this);
    }

    @Override
    public @NotNull CorePermission getMoveOwnPermission() {
        return this.moveOwnPermission;
    }

    @Override
    public @NotNull CorePermission getMoveOtherPermission() {
        return this.moveOtherPermission;
    }

    @Override
    public @NotNull CorePermission getMakePermission() {
        return this.makePermission;
    }

    @Override
    public @NotNull SpecialBlocksRequirement getSpecialBlocksRequirement() {
        return this.specialBlocksRequirement;
    }
}
