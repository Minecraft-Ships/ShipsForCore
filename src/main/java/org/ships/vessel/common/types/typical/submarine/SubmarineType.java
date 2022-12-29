package org.ships.vessel.common.types.typical.submarine;

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
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.assits.shiptype.FuelledShipType;
import org.ships.vessel.common.assits.shiptype.SpecialBlockShipType;
import org.ships.vessel.common.requirement.FuelRequirement;
import org.ships.vessel.common.requirement.Requirement;
import org.ships.vessel.common.requirement.SpecialBlocksRequirement;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class SubmarineType extends AbstractShipType<Submarine>
        implements SpecialBlockShipType<Submarine>, FuelledShipType<Submarine> {

    private final Collection<Requirement<?>> requirements = new HashSet<>();

    public SubmarineType() {
        this("Submarine", new File(ShipsPlugin.getPlugin().getConfigFolder(),
                                   "/Configuration/ShipType/Submarine." + TranslateCore
                                           .getPlatform()
                                           .getConfigFormat()
                                           .getFileType()[0]));
    }

    public SubmarineType(String name, File file) {
        this(ShipsPlugin.getPlugin(), name,
             TranslateCore.createConfigurationFile(file, TranslateCore.getPlatform().getConfigFormat()), BlockTypes.AIR,
             BlockTypes.WATER);
    }

    public SubmarineType(Plugin plugin,
                         String displayName,
                         ConfigurationStream.ConfigurationFile file,
                         BlockType... types) {
        super(plugin, displayName, file, types);
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
        if (this.requirements.isEmpty()) {
            Requirement<?> fuelRequirement = new FuelRequirement(null, this.getDefaultFuelSlot(),
                                                                 this.getDefaultFuelConsumption(),
                                                                 this.getDefaultFuelTypes());
            Requirement<?> specialBlocksRequirement = new SpecialBlocksRequirement(null,
                                                                                   this.getDefaultSpecialBlocksPercent(),
                                                                                   this.getDefaultSpecialBlockTypes());
            this.requirements.add(fuelRequirement);
            this.requirements.add(specialBlocksRequirement);
        }
        return this.requirements;
    }

    @Override
    public @NotNull Submarine createNewVessel(@NotNull SignTileEntity ste, @NotNull SyncBlockPosition bPos) {
        return new Submarine(this, ste, bPos);
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
}
