package org.ships.vessel.common.types.typical.airship;

import org.array.utils.ArrayUtils;
import org.core.TranslateCore;
import org.core.config.ConfigurationStream;
import org.core.inventory.item.ItemTypes;
import org.core.inventory.item.type.post.ItemTypes1V13;
import org.core.permission.CorePermission;
import org.core.platform.plugin.Plugin;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.grouptype.versions.BlockGroups1V13;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.assits.shiptype.FuelledShipType;
import org.ships.vessel.common.assits.shiptype.SpecialBlockShipType;
import org.ships.vessel.common.requirement.*;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class AirshipType extends AbstractShipType<Airship>
        implements CloneableShipType<Airship>, SpecialBlockShipType<Airship>, FuelledShipType<Airship> {

    private FuelRequirement fuelRequirement;
    private SpecialBlocksRequirement specialBlocksRequirement;
    private SpecialBlockRequirement burnerRequirement;
    private MinSizeRequirement minSizeRequirement;
    private MaxSizeRequirement maxSizeRequirement;

    public AirshipType() {
        this("Airship", new File(ShipsPlugin.getPlugin().getConfigFolder(),
                                 "/Configuration/ShipType/Airship." + TranslateCore
                                         .getPlatform()
                                         .getConfigFormat()
                                         .getFileType()[0]));
    }

    public AirshipType(String displayName, File file) {
        this(ShipsPlugin.getPlugin(), displayName,
             TranslateCore.createConfigurationFile(file, TranslateCore.getPlatform().getConfigFormat()),
             BlockTypes.AIR);
    }

    public AirshipType(Plugin plugin,
                       String displayName,
                       ConfigurationStream.ConfigurationFile file,
                       BlockType... types) {
        super(plugin, displayName, file, types);
    }

    public @NotNull FuelRequirement getFuelRequirement() {
        return this.fuelRequirement;
    }

    public @NotNull SpecialBlockRequirement getBurnerRequirement() {
        return this.burnerRequirement;
    }

    public @NotNull SpecialBlocksRequirement getSpecialBlocksRequirement() {
        return this.specialBlocksRequirement;
    }

    public @NotNull MinSizeRequirement getMinimumSizeRequirement() {
        return this.minSizeRequirement;
    }

    public @NotNull MaxSizeRequirement getMaxSizeRequirement() {
        return this.maxSizeRequirement;
    }

    public boolean isUsingBurner() {
        return this.file.getBoolean(BURNER_BLOCK).orElse(true);
    }

    @Override
    public CloneableShipType<Airship> cloneWithName(File file, String name) {
        return new AirshipType(name, file);
    }

    @Override
    public CloneableShipType<Airship> getOriginType() {
        return ShipType.AIRSHIP;
    }

    @Override
    protected void createDefault(ConfigurationStream.@NotNull ConfigurationFile file) {
        this.file.set(BURNER_BLOCK, true);
        this.file.set(SPECIAL_BLOCK_PERCENT, 60.0f);
        this.file.set(SPECIAL_BLOCK_TYPE, ArrayUtils.ofSet(BlockGroups1V13.WOOL.getGrouped()));
        this.file.set(FUEL_CONSUMPTION, 1);
        this.file.set(FUEL_SLOT, FuelSlot.BOTTOM);
        this.file.set(FUEL_TYPES, ArrayUtils.ofSet(ItemTypes.COAL.get(), ItemTypes1V13.CHARCOAL.get()));
        this.file.set(MAX_SPEED, 10);
        this.file.set(ALTITUDE_SPEED, 5);
    }

    @Override
    public Collection<Requirement<?>> getDefaultRequirements() {
        if (this.fuelRequirement == null) {
            this.fuelRequirement = new FuelRequirement(null, this.getDefaultFuelSlot(),
                                                       this.getDefaultFuelConsumption(), this.getDefaultFuelTypes());
        }
        if (this.burnerRequirement == null) {
            this.burnerRequirement = new SpecialBlockRequirement(null, BlockTypes.FIRE, this.isUsingBurner() ? 1 : 0,
                                                                 "Burner");
        }
        if (this.specialBlocksRequirement == null) {
            this.specialBlocksRequirement = new SpecialBlocksRequirement(null, this.getDefaultSpecialBlocksPercent(),
                                                                         this.getDefaultSpecialBlockTypes());
        }
        if (this.minSizeRequirement == null) {
            this.minSizeRequirement = new MinSizeRequirement(null, this.getDefaultMinSize());
        }
        if (this.maxSizeRequirement == null) {
            this.maxSizeRequirement = new MaxSizeRequirement(null, this.getDefaultMaxSize().orElse(null));
        }
        return List.of(this.burnerRequirement, this.fuelRequirement, this.specialBlocksRequirement,
                       this.minSizeRequirement, this.maxSizeRequirement);
    }

    @Override
    public @NotNull Airship createNewVessel(@NotNull SignTileEntity ste, @NotNull SyncBlockPosition bPos) {
        return new Airship(this, ste, bPos);
    }

    @Override
    public @NotNull CorePermission getMoveOwnPermission() {
        return Permissions.AIRSHIP_MOVE_OWN;
    }

    @Override
    public @NotNull CorePermission getMoveOtherPermission() {
        return Permissions.AIRSHIP_MOVE_OTHER;
    }

    @Override
    public @NotNull CorePermission getMakePermission() {
        return Permissions.AIRSHIP_MAKE;
    }


}
