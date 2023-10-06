package org.ships.vessel.common.types.typical.watership;

import org.array.utils.ArrayUtils;
import org.core.TranslateCore;
import org.core.config.ConfigurationStream;
import org.core.permission.CorePermission;
import org.core.platform.plugin.Plugin;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.block.grouptype.versions.BlockGroups1V13;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.assits.shiptype.SpecialBlocksShipType;
import org.ships.vessel.common.requirement.MaxSizeRequirement;
import org.ships.vessel.common.requirement.MinSizeRequirement;
import org.ships.vessel.common.requirement.Requirement;
import org.ships.vessel.common.requirement.SpecialBlocksRequirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class WaterShipType extends AbstractShipType<WaterShip>
        implements CloneableShipType<WaterShip>, SpecialBlocksShipType<WaterShip> {

    public static final String NAME = "Ship";
    private CorePermission moveOwnPermission = Permissions.WATERSHIP_MOVE_OWN;
    private CorePermission moveOtherPermission = Permissions.WATERSHIP_MOVE_OTHER;
    private CorePermission makePermission = Permissions.WATERSHIP_MAKE;


    private SpecialBlocksRequirement specialBlocksRequirement;
    private MinSizeRequirement minSizeRequirement;
    private MaxSizeRequirement maxSizeRequirement;

    private Integer maxSize;
    private int minSize;

    public WaterShipType() {
        this(NAME, new File(ShipsPlugin.getPlugin().getConfigFolder(),
                            "/Configuration/ShipType/Watership." + TranslateCore
                                    .getPlatform()
                                    .getConfigFormat()
                                    .getFileType()[0]));
    }

    public WaterShipType(@NotNull String name, @NotNull File file) {
        this(ShipsPlugin.getPlugin(), name,
             TranslateCore.createConfigurationFile(file, TranslateCore.getPlatform().getConfigFormat()), BlockTypes.AIR,
             BlockTypes.WATER);
    }

    public WaterShipType(@NotNull Plugin plugin,
                         @NotNull String displayName,
                         @NotNull ConfigurationStream.ConfigurationFile file,
                         BlockType... types) {
        super(plugin, displayName, file, types);
        file.getInteger(MaxSizeRequirement.MAX_SIZE).ifPresent(v -> maxSize = v);
        this.minSize = file.getInteger(MinSizeRequirement.MIN_SIZE, 0);
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

    @Override
    public @NotNull WaterShip createNewVessel(@NotNull SignSide side, @NotNull SyncBlockPosition bPos) {
        return new WaterShip(side, bPos, this);
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
    protected void createDefault(@NotNull ConfigurationStream.ConfigurationFile file) {
        this.file.set(MAX_SPEED, 10);
        this.file.set(ALTITUDE_SPEED, 5);
        this.file.set(SPECIAL_BLOCK_PERCENT, 25);
        this.file.set(SPECIAL_BLOCK_TYPE, ArrayUtils.ofSet(BlockGroups1V13.WOOL.getGrouped()));
    }

    @Override
    public Collection<Requirement<?>> getDefaultRequirements() {
        if (this.maxSizeRequirement == null) {
            this.maxSizeRequirement = new MaxSizeRequirement(null, this.maxSize);
        }
        if (this.minSizeRequirement == null) {
            this.minSizeRequirement = new MinSizeRequirement(null, this.minSize);
        }
        if (this.specialBlocksRequirement == null) {
            this.specialBlocksRequirement = new SpecialBlocksRequirement(null, this.getDefaultSpecialBlocksPercent(),
                                                                         this.getDefaultSpecialBlockTypes());
        }
        return List.of(this.minSizeRequirement, this.maxSizeRequirement, this.specialBlocksRequirement);
    }

    @Override
    public CloneableShipType<WaterShip> cloneWithName(@NotNull File file, @NotNull String name) {
        return new WaterShipType(name, file);
    }

    @Override
    public CloneableShipType<WaterShip> getOriginType() {
        return ShipType.WATERSHIP;
    }

    @Override
    public @NotNull SpecialBlocksRequirement getSpecialBlocksRequirement() {
        return this.specialBlocksRequirement;
    }
}
