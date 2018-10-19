package org.ships.vessel.common.loader;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.core.text.TextColours;
import org.core.world.WorldExtent;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.AbstractShipsVessel;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ShipsFileLoader implements ShipsLoader {

    protected File file;
    protected static final String[] SPEED_MAX = {"Speed", "Max"};
    protected static final String[] SPEED_ALTITUDE = {"Speed", "Altitude"};
    protected static final String[] META_LOCATION_X = {"Meta", "Location", "X"};
    protected static final String[] META_LOCATION_Y = {"Meta", "Location", "Y"};
    protected static final String[] META_LOCATION_Z = {"Meta", "Location", "Z"};
    protected static final String[] META_LOCATION_WORLD = {"Meta", "Location", "World"};

    public ShipsFileLoader(File file){
        this.file = file;
    }

    public void save(AbstractShipsVessel vessel){
        ConfigurationFile file = CorePlugin.createConfigurationFile(this.file, ConfigurationLoaderTypes.YAML);
        Map<ConfigurationNode, String> map = new HashMap<>(vessel.serialize(file));
        map.put(new ConfigurationNode(SPEED_MAX), vessel.getMaxSpeed() + "");
        map.put(new ConfigurationNode(SPEED_ALTITUDE), vessel.getAltitudeSpeed() + "");
        map.put(new ConfigurationNode(META_LOCATION_WORLD), vessel.getPosition().getWorld().getPlatformUniquieId());
        map.put(new ConfigurationNode(META_LOCATION_X), vessel.getPosition().getX() + "");
        map.put(new ConfigurationNode(META_LOCATION_Y), vessel.getPosition().getY() + "");
        map.put(new ConfigurationNode(META_LOCATION_Z), vessel.getPosition().getZ() + "");
    }

    @Override
    public Vessel load() throws IOException {
        ConfigurationFile file = CorePlugin.createConfigurationFile(this.file, ConfigurationLoaderTypes.YAML);
        Optional<WorldExtent> opWorld = file.parse(new ConfigurationNode(META_LOCATION_WORLD), Parser.STRING_TO_WORLD);
        if(!opWorld.isPresent()){
            throw new IOException("Unknown World");
        }
        Optional<Integer> opX = file.parse(new ConfigurationNode(META_LOCATION_X), Parser.STRING_TO_INTEGER);
        if(!opX.isPresent()){
            throw new IOException("Unknown X value");
        }
        Optional<Integer> opY = file.parse(new ConfigurationNode(META_LOCATION_Y), Parser.STRING_TO_INTEGER);
        if(!opY.isPresent()){
            throw new IOException("Unknown Y value");
        }
        Optional<Integer> opZ = file.parse(new ConfigurationNode(META_LOCATION_Z), Parser.STRING_TO_INTEGER);
        if(!opZ.isPresent()){
            throw new IOException("Unknown Z value");
        }
        BlockPosition position = opWorld.get().getPosition(opX.get(), opY.get(), opZ.get());
        LicenceSign sign = ShipsPlugin.getPlugin().get(LicenceSign.class).get();
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if(!(opTile.isPresent() && opTile.get() instanceof LiveSignTileEntity)){
            throw new IOException("LicenceSign is not at location " + position.getX() + "," + position.getY() + "," + position.getZ() + "," + position.getWorld().getName());
        }
        LiveSignTileEntity lste = (LiveSignTileEntity)opTile.get();
        if(!sign.isSign(lste)){
            throw new IOException("LicenceSign is not at location " + position.getX() + "," + position.getY() + "," + position.getZ() + "," + position.getWorld().getName());
        }

        String shipTypeS = TextColours.stripColours(lste.getLine(1));
        Optional<ShipType> opShipType = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(s -> s.getDisplayName().equals(shipTypeS)).findAny();
        if(!opShipType.isPresent()){
            throw new IOException("Unknown ShipType");
        }
        ShipType type = opShipType.get();
        Vessel vessel = type.createNewVessel(lste);
        if(!(vessel instanceof AbstractShipsVessel)){
            throw new IOException("ShipType requires to be AbstractShipsVessel");
        }
        AbstractShipsVessel ship = (AbstractShipsVessel)vessel;

        //SET DEFAULTS

        ship.deserializeExtra(file);
        return ship;
    }
}
