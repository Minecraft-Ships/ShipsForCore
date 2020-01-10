package org.ships.vessel.converts.vessel.shipsfive;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.core.world.position.BlockPosition;
import org.core.world.position.ExactPosition;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.common.types.typical.airship.Airship;
import org.ships.vessel.converts.vessel.VesselConverter;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class Ships5VesselConverter implements VesselConverter<ShipsVessel> {
    @Override
    public File getFolder() {
        return new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "VesselData");
    }

    @Override
    public ShipsVessel convert(File file) throws IOException {
        ConfigurationFile config = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.DEFAULT);

        String type = config.parseString(new ConfigurationNode("ShipsData", "Type")).get();
        Integer percent = config.parseInt(new ConfigurationNode("ShipsData", "Config", "Block", "Percent")).orElse(null);
        Integer consumption = config.parseInt(new ConfigurationNode("ShipsData", "Config", "Fuel", "Consumption")).orElse(null);
        int maxBlocks = config.parseInt(new ConfigurationNode("ShipsData", "Config", "Block", "Max")).get();
        int minBlocks = config.parseInt(new ConfigurationNode("ShipsData", "Config", "Block", "Min")).get();
        int engineSpeed = config.parseInt(new ConfigurationNode("ShipsData", "Config", "Speed", "Engine")).get();
        UUID owner = config.parse(new ConfigurationNode("ShipsData", "Player", "Name"), Parser.STRING_TO_UNIQUIE_ID).get();
        BlockPosition blockPosition = config.parse(new ConfigurationNode("ShipsData", "Location", "Sign"), Parser.STRING_TO_BLOCK_POSITION).get();
        ExactPosition teleportPosition = config.parse(new ConfigurationNode("ShipsData", "Location", "Teleport"), Parser.STRING_TO_EXACT_POSITION).get();

        Optional<LiveTileEntity> opTile = blockPosition.getTileEntity();
        if(!opTile.isPresent()){
            throw new IOException("Unable to locate licence sign");
        }
        LiveTileEntity lte = opTile.get();
        if(!(lte instanceof LiveSignTileEntity)){
            throw new IOException("Unable to locate licence sign");
        }
        LiveSignTileEntity lste = (LiveSignTileEntity)lte;
        ShipsVessel vessel;
        switch (type){
            case "Airship":
                vessel = ShipType.AIRSHIP.createNewVessel(lste);
                if(consumption != null) {
                    ((Airship) vessel).setFuelConsumption(consumption);
                }
                if(percent != null) {
                    ((Airship) vessel).setSpecialBlockPercent(percent);
                }
                break;
            default:
                throw new IOException("Unknown ships 5 type of " + type);
        }
        if(vessel != null){
            vessel.setTeleportPosition(teleportPosition);
            vessel.setMaxSpeed(engineSpeed);
            vessel.getCrew().put(owner, CrewPermission.CAPTAIN);
            ShipsPlugin.getPlugin().registerVessel(vessel);
            ShipsPlugin.getPlugin().getConfig().getDefaultFinder().setConnectedVessel(vessel).getConnectedBlocksOvertime(vessel.getPosition(), new OvertimeBlockFinderUpdate() {
                @Override
                public void onShipsStructureUpdated(PositionableShipsStructure structure) {
                    vessel.setStructure(structure);
                    vessel.setLoading(false);
                }

                @Override
                public boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                    return true;
                }
            });
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
