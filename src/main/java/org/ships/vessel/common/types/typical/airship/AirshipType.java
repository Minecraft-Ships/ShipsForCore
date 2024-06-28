package org.ships.vessel.common.types.typical.airship;

import org.core.TranslateCore;
import org.core.config.ConfigurationStream;
import org.core.inventory.item.ItemTypes;
import org.core.inventory.item.type.post.ItemTypes1V13;
import org.core.permission.CorePermission;
import org.core.platform.plugin.Plugin;
import org.core.utils.Identifiable;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.block.grouptype.BlockGroups;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.assits.shiptype.FuelledShipType;
import org.ships.vessel.common.assits.shiptype.SizedShipType;
import org.ships.vessel.common.assits.shiptype.SpecialBlocksShipType;
import org.ships.vessel.common.requirement.*;
import org.ships.vessel.common.types.ShipTypes;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AirshipType extends AbstractShipType<Airship>
        implements CloneableShipType<Airship>, SpecialBlocksShipType<Airship>, FuelledShipType<Airship>,
        SizedShipType<Airship> {

    private FuelRequirement fuelRequirement;
    private SpecialBlocksRequirement specialBlocksRequirement;
    private SpecialBlockRequirement burnerRequirement;
    private MinSizeRequirement minSizeRequirement;
    private MaxSizeRequirement maxSizeRequirement;

    private int min;
    private Integer max;

    public AirshipType() {
        this("Airship", new File(ShipsPlugin.getPlugin().getConfigFolder(),
                                 "/Configuration/ShipType/Airship." + TranslateCore
                                         .getPlatform()
                                         .getConfigFormat()
                                         .getFileType()[0]));
    }

    public AirshipType(String displayName, File file) {
        this(ShipsPlugin.getPlugin(), displayName,
             TranslateCore.getConfigManager().read(file, TranslateCore.getPlatform().getConfigFormat()),
             BlockTypes.AIR);
    }

    public AirshipType(Plugin plugin,
                       String displayName,
                       ConfigurationStream.ConfigurationFile file,
                       BlockType... types) {
        super(plugin, displayName, file, types);
        this.min = file.getInteger(MinSizeRequirement.MIN_SIZE, 0);
        file.getInteger(MaxSizeRequirement.MAX_SIZE).ifPresent(value -> this.max = value);
    }

    @Override
    public @NotNull FuelRequirement getFuelRequirement() {
        return this.fuelRequirement;
    }

    public @NotNull SpecialBlockRequirement getBurnerRequirement() {
        return this.burnerRequirement;
    }

    @Override
    public @NotNull SpecialBlocksRequirement getSpecialBlocksRequirement() {
        return this.specialBlocksRequirement;
    }

    @Override
    public @NotNull MinSizeRequirement getMinimumSizeRequirement() {
        return this.minSizeRequirement;
    }

    @Override
    public @NotNull MaxSizeRequirement getMaximumSizeRequirement() {
        return this.maxSizeRequirement;
    }

    public boolean isUsingBurner() {
        return this.file.getBoolean(BURNER_BLOCK).orElse(true);
    }

    @Override
    public AirshipType cloneWithName(File file, String name) {
        return new AirshipType(name, file);
    }

    @Override
    public AirshipType getOriginType() {
        return ShipTypes.AIRSHIP;
    }

    @Override
    protected void createDefault(ConfigurationStream.@NotNull ConfigurationFile file) {
        this.file.set(BURNER_BLOCK, true);
        this.file.set(SPECIAL_BLOCK_PERCENT, 60.0f);
        this.file.set(SPECIAL_BLOCK_TYPE, BlockGroups.WOOL
                .get()
                .getBlocks()
                .sorted(Comparator.comparing(Identifiable::getId))
                .collect(Collectors.toList()));
        this.file.set(FUEL_CONSUMPTION, 1);
        this.file.set(FUEL_SLOT, FuelSlot.BOTTOM);
        this.file.set(FUEL_TYPES, Set.of(ItemTypes.COAL.get(), ItemTypes1V13.CHARCOAL.get()));
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
            this.minSizeRequirement = new MinSizeRequirement(null, this.min);
        }
        if (this.maxSizeRequirement == null) {
            this.maxSizeRequirement = new MaxSizeRequirement(null, this.max);
        }
        return List.of(this.burnerRequirement, this.fuelRequirement, this.specialBlocksRequirement,
                       this.minSizeRequirement, this.maxSizeRequirement);
    }

    @Override
    public @NotNull Airship createNewVessel(@NotNull SignSide side, @NotNull SyncBlockPosition bPos) {
        return new Airship(side, bPos, this);
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
