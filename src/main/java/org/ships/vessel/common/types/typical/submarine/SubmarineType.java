package org.ships.vessel.common.types.typical.submarine;

import org.core.TranslateCore;
import org.core.config.ConfigurationStream;
import org.core.inventory.item.ItemTypes;
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
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.shiptype.FuelledShipType;
import org.ships.vessel.common.assits.shiptype.SpecialBlocksShipType;
import org.ships.vessel.common.requirement.*;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SubmarineType extends AbstractShipType<Submarine>
        implements SpecialBlocksShipType<Submarine>, FuelledShipType<Submarine> {


    private final int min;
    private FuelRequirement fuelRequirement;
    private SpecialBlocksRequirement specialBlocksRequirement;
    private MinSizeRequirement minSizeRequirement;
    private MaxSizeRequirement maxSizeRequirement;
    private @Nullable Integer max;

    public SubmarineType() {
        this("Submarine", new File(ShipsPlugin.getPlugin().getConfigFolder(),
                                   "/Configuration/ShipType/Submarine." + TranslateCore
                                           .getPlatform()
                                           .getConfigFormat()
                                           .getFileType()[0]));
    }

    public SubmarineType(String displayName, File file) {
        this(ShipsPlugin.getPlugin(), displayName,
             TranslateCore.createConfigurationFile(file, TranslateCore.getPlatform().getConfigFormat()), BlockTypes.AIR,
             BlockTypes.WATER);
    }

    public SubmarineType(Plugin plugin,
                         String displayName,
                         ConfigurationStream.ConfigurationFile file,
                         BlockType... types) {
        super(plugin, displayName, file, types);
        this.min = file.getInteger(MinSizeRequirement.MIN_SIZE, 0);
        file.getInteger(MaxSizeRequirement.MAX_SIZE).ifPresent(value -> this.max = value);
    }

    @Override
    protected void createDefault(ConfigurationStream.@NotNull ConfigurationFile file) {
        this.file.set(SPECIAL_BLOCK_PERCENT, 75.0);
        this.file.set(SPECIAL_BLOCK_TYPE, Collections.singleton(BlockTypes.IRON_BLOCK));
        this.file.set(FUEL_CONSUMPTION, 1);
        this.file.set(FUEL_SLOT, FuelSlot.BOTTOM);
        this.file.set(FUEL_TYPES, Collections.singleton(ItemTypes.COAL_BLOCK.get()));
        this.file.set(MAX_SPEED, 10);
        this.file.set(ALTITUDE_SPEED, 5);
    }

    @Override
    public Collection<Requirement<?>> getDefaultRequirements() {
        if (this.fuelRequirement == null) {
            this.fuelRequirement = new FuelRequirement(null, this.getDefaultFuelSlot(),
                                                       this.getDefaultFuelConsumption(), this.getDefaultFuelTypes());
        }
        if (this.specialBlocksRequirement == null) {
            this.specialBlocksRequirement = new SpecialBlocksRequirement(null, this.getDefaultSpecialBlocksPercent(),
                                                                         this.getDefaultSpecialBlockTypes());
        }
        if (this.minSizeRequirement == null) {
            this.minSizeRequirement = new MinSizeRequirement(null, this.min);
        }
        if (this.maxSizeRequirement == null) {
            this.maxSizeRequirement = new MaxSizeRequirement(null, this.max);
        }
        return List.of(this.specialBlocksRequirement, this.fuelRequirement);
    }

    @Override
    public @NotNull Submarine createNewVessel(@NotNull SignSide side, @NotNull SyncBlockPosition bPos) {
        return new Submarine(side, bPos, this);
    }

    @Override
    public @NotNull CorePermission getMoveOwnPermission() {
        return Permissions.SUBMARINE_MOVE_OWN;
    }

    @Override
    public @NotNull CorePermission getMoveOtherPermission() {
        return Permissions.SUBMARINE_MOVE_OTHER;
    }

    @Override
    public @NotNull CorePermission getMakePermission() {
        return Permissions.SUBMARINE_MAKE;
    }

    @Override
    public FuelRequirement getFuelRequirement() {
        return this.fuelRequirement;
    }

    @Override
    public @NotNull SpecialBlocksRequirement getSpecialBlocksRequirement() {
        return this.specialBlocksRequirement;
    }
}
