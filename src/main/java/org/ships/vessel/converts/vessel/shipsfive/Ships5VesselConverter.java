package org.ships.vessel.converts.vessel.shipsfive;

import org.core.TranslateCore;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncExactPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.common.types.typical.airship.Airship;
import org.ships.vessel.converts.vessel.VesselConverter;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class Ships5VesselConverter implements VesselConverter<ShipsVessel> {
    @Override
    public @NotNull File getFolder() {
        return new File(ShipsPlugin.getPlugin().getConfigFolder(), "VesselData");
    }

    @Override
    public @NotNull ShipsVessel convert(@NotNull File file) throws IOException {
        ConfigurationStream.ConfigurationFile config = TranslateCore.createConfigurationFile(file, TranslateCore
                .getPlatform()
                .getConfigFormat());

        String type = config.getString(new ConfigurationNode("ShipsData", "Type")).get();
        Integer percent = config
                .getInteger(new ConfigurationNode("ShipsData", "Config", "Block", "Percent"))
                .orElse(null);
        Integer consumption = config
                .getInteger(new ConfigurationNode("ShipsData", "Config", "Fuel", "Consumption"))
                .orElse(null);
        int maxBlocks = config.getInteger(new ConfigurationNode("ShipsData", "Config", "Block", "Max")).get();
        int minBlocks = config.getInteger(new ConfigurationNode("ShipsData", "Config", "Block", "Min")).get();
        int engineSpeed = config.getInteger(new ConfigurationNode("ShipsData", "Config", "Speed", "Engine")).get();
        UUID owner = config
                .parse(new ConfigurationNode("ShipsData", "Player", "Name"), Parser.STRING_TO_UNIQUE_ID)
                .get();
        SyncBlockPosition blockPosition = config
                .parse(new ConfigurationNode("ShipsData", "Location", "Sign"), Parser.STRING_TO_BLOCK_POSITION)
                .get();
        SyncExactPosition teleportPosition = config
                .parse(new ConfigurationNode("ShipsData", "Location", "Teleport"), Parser.STRING_TO_EXACT_POSITION)
                .get();

        Optional<LiveTileEntity> opTile = blockPosition.getTileEntity();
        if (opTile.isEmpty()) {
            throw new IOException("Unable to locate licence sign");
        }
        LiveTileEntity lte = opTile.get();
        if (!(lte instanceof LiveSignTileEntity)) {
            throw new IOException("Unable to locate licence sign");
        }
        LiveSignTileEntity lste = (LiveSignTileEntity)lte;
        ShipsVessel vessel;
        switch (type) {
            case "Airship":
                vessel = ShipType.AIRSHIP.createNewVessel(lste);
                if (consumption != null) {
                    ((Airship) vessel).setFuelConsumption(consumption);
                }
                if (percent != null) {
                    ((Airship) vessel).setSpecialBlocksPercent(percent.floatValue());
                }
                break;
            default:
                throw new IOException("Unknown ships 5 type of " + type);
        }
        if (vessel != null) {
            vessel.setTeleportPosition(teleportPosition);
            vessel.setMaxSpeed(engineSpeed);
            vessel.getCrew().put(owner, CrewPermission.CAPTAIN);
            ShipsPlugin.getPlugin().registerVessel(vessel);
            vessel.updateStructure().thenAccept(structure -> vessel.setLoading(false));
            return vessel;
        }
        throw new IOException("Unknown");
    }

    @Override
    public String getId() {
        return "ships:five_to_r2_six";
    }

    @Override
    public String getName() {
        return "Five to -R2 6.0.0.0";
    }
}
