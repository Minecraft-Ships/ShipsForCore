package org.ships.vessel.common.loader.shipsvessel;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.core.text.Text;
import org.core.vector.types.Vector3Int;
import org.core.world.WorldExtent;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.config.parsers.ShipsParsers;
import org.ships.exceptions.load.FileLoadVesselException;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.WrappedFileLoadVesselException;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.loader.ShipsLoader;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class ShipsFileLoader implements ShipsLoader {

    protected File file;
    protected static final String[] SPEED_MAX = {"Speed", "Max"};
    protected static final String[] SPEED_ALTITUDE = {"Speed", "Altitude"};
    protected static final String[] META_DEFAULT_PERMISSION = {"Meta", "Permission", "Default"};
    protected static final String[] META_LOCATION_X = {"Meta", "Location", "X"};
    protected static final String[] META_LOCATION_Y = {"Meta", "Location", "Y"};
    protected static final String[] META_LOCATION_Z = {"Meta", "Location", "Z"};
    protected static final String[] META_LOCATION_WORLD = {"Meta", "Location", "World"};
    @Deprecated
    protected static final String[] META_LOCATION_TELEPORT_X = {"Meta", "Location", "Teleport", "X"};
    @Deprecated
    protected static final String[] META_LOCATION_TELEPORT_Y = {"Meta", "Location", "Teleport", "Y"};
    @Deprecated
    protected static final String[] META_LOCATION_TELEPORT_Z = {"Meta", "Location", "Teleport", "Z"};
    @Deprecated
    protected static final String[] META_LOCATION_TELEPORT_WORLD = {"Meta", "Location", "Teleport", "World"};
    protected static final String[] META_STRUCTURE = {"Meta", "Location", "Structure"};
    protected static final String[] META_FLAGS = {"Meta", "Flags"};

    private static class StructureLoad {

        private ShipsVessel ship;

        public StructureLoad(ShipsVessel shipsVessel){
            this.ship = shipsVessel;
        }

        public void load(SyncBlockPosition position, List<Vector3Int> structureList) {
            if (structureList.isEmpty()) {
                ShipsPlugin.getPlugin().getConfig().getDefaultFinder().getConnectedBlocksOvertime(position, new OvertimeBlockFinderUpdate() {
                    @Override
                    public void onShipsStructureUpdated(PositionableShipsStructure structure) {
                        ship.setStructure(structure);
                        ship.setLoading(false);
                        CorePlugin.getConsole().sendMessagePlain(ship.getId() + " has loaded.");
                    }

                    @Override
                    public boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                        return true;
                    }
                });
            } else {
                PositionableShipsStructure pss = ship.getStructure();
                pss.setRaw(structureList);
                ship.setLoading(false);
                CorePlugin.getConsole().sendMessagePlain(ship.getId() + " has loaded.");
            }
        }
    }

    public ShipsFileLoader(File file) {
        this.file = file;
    }

    public void save(AbstractShipsVessel vessel) {
        ConfigurationFile file = CorePlugin.createConfigurationFile(this.file, ConfigurationLoaderTypes.DEFAULT);
        Map<CrewPermission, List<String>> uuidList = new HashMap<>();
        vessel.getCrew().forEach((key, value) -> {
            List<String> list = uuidList.get(value);
            boolean override = false;
            if (list == null) {
                override = true;
                list = new ArrayList<>();
            }
            list.add(key.toString());
            if (override) {
                uuidList.put(value, list);
            } else {
                uuidList.replace(value, list);
            }
        });
        Map<ConfigurationNode, Object> map = new HashMap<>(vessel.serialize(file));
        map.put(new ConfigurationNode(SPEED_MAX), vessel.getMaxSpeed());
        map.put(new ConfigurationNode(SPEED_ALTITUDE), vessel.getAltitudeSpeed());
        map.put(new ConfigurationNode(META_LOCATION_WORLD), vessel.getPosition().getWorld().getPlatformUniquieId());
        map.put(new ConfigurationNode(META_LOCATION_X), vessel.getPosition().getX());
        map.put(new ConfigurationNode(META_LOCATION_Y), vessel.getPosition().getY());
        map.put(new ConfigurationNode(META_LOCATION_Z), vessel.getPosition().getZ());
        vessel.getTeleportPositions().entrySet().forEach(e -> {
            map.put(new ConfigurationNode("Meta", "Location", "Teleport", e.getKey(), "X"), (vessel.getPosition().getX() - e.getValue().getX()));
            map.put(new ConfigurationNode("Meta", "Location", "Teleport", e.getKey(), "Y"), (vessel.getPosition().getY() - e.getValue().getY()));
            map.put(new ConfigurationNode("Meta", "Location", "Teleport", e.getKey(), "Z"), (vessel.getPosition().getZ() - e.getValue().getZ()));
            map.put(new ConfigurationNode("Meta", "Location", "Teleport", e.getKey(), "World"), e.getValue().getWorld().getPlatformUniquieId());
        });
        uuidList.forEach((key, value) -> map.put(new ConfigurationNode("Meta", "Permission", key.getId()), value));
        map.forEach(file::set);
        file.set(new ConfigurationNode(META_STRUCTURE), Parser.STRING_TO_VECTOR3INT, vessel.getStructure().getRelativePositions());
        file.set(new ConfigurationNode(META_FLAGS), ShipsParsers.STRING_TO_VESSEL_FLAG, CorePlugin.arrayCast(f -> f instanceof VesselFlag.Serializable, vessel.getFlags()));
        file.save();
    }

    @Override
    public ShipsVessel load() throws LoadVesselException {
        ConfigurationFile file = CorePlugin.createConfigurationFile(this.file, ConfigurationLoaderTypes.DEFAULT);
        Optional<WorldExtent> opWorld = file.parse(new ConfigurationNode(META_LOCATION_WORLD), Parser.STRING_TO_WORLD);
        if (!opWorld.isPresent()) {
            throw new FileLoadVesselException(this.file, "Unknown World of " + file.parseString(new ConfigurationNode(META_LOCATION_WORLD)).orElse("'No value found'"));
        }
        Optional<Integer> opX = file.parse(new ConfigurationNode(META_LOCATION_X), Parser.STRING_TO_INTEGER);
        if (!opX.isPresent()) {
            throw new FileLoadVesselException(this.file, "Unknown X value");
        }
        Optional<Integer> opY = file.parse(new ConfigurationNode(META_LOCATION_Y), Parser.STRING_TO_INTEGER);
        if (!opY.isPresent()) {
            throw new FileLoadVesselException(this.file, "Unknown Y value");
        }
        Optional<Integer> opZ = file.parse(new ConfigurationNode(META_LOCATION_Z), Parser.STRING_TO_INTEGER);
        if (!opZ.isPresent()) {
            throw new FileLoadVesselException(this.file, "Unknown Z value");
        }
        SyncBlockPosition position = opWorld.get().getPosition(opX.get(), opY.get(), opZ.get());
        LicenceSign sign = ShipsPlugin.getPlugin().get(LicenceSign.class).get();
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if (!(opTile.isPresent() && opTile.get() instanceof LiveSignTileEntity)) {
            throw new FileLoadVesselException(this.file, "LicenceSign is not at location " + position.getX() + "," + position.getY() + "," + position.getZ() + "," + position.getWorld().getName() + ": Error V1");
        }
        LiveSignTileEntity lste = (LiveSignTileEntity) opTile.get();
        if (!sign.isSign(lste)) {
            throw new FileLoadVesselException(this.file, "LicenceSign is not at location " + position.getX() + "," + position.getY() + "," + position.getZ() + "," + position.getWorld().getName() + ": Error V2");
        }

        Optional<Text> opShipTypeS = lste.getLine(1);
        if (!opShipTypeS.isPresent()) {
            throw new FileLoadVesselException(this.file, "LicenceSign is not at location " + position.getX() + "," + position.getY() + "," + position.getZ() + "," + position.getWorld().getName() + ": Error V3");
        }
        String shipTypeS = opShipTypeS.get().toPlain();
        Optional<ShipType> opShipType = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(s -> s.getDisplayName().equals(shipTypeS)).findAny();
        if (!opShipType.isPresent()) {
            throw new FileLoadVesselException(this.file, "Unknown ShipType");
        }
        ShipType type = opShipType.get();
        Vessel vessel = type.createNewVessel(lste);
        if (!(vessel instanceof ShipsVessel)) {
            throw new FileLoadVesselException(this.file, "ShipType requires to be ShipsVessel");
        }
        ShipsVessel ship = (ShipsVessel) vessel;

        file.parseInt(new ConfigurationNode(SPEED_ALTITUDE)).ifPresent(ship::setAltitudeSpeed);
        file.parseInt(new ConfigurationNode(SPEED_MAX)).ifPresent(ship::setMaxSpeed);

        Optional<Double> opTelX = file.parseDouble(new ConfigurationNode(META_LOCATION_TELEPORT_X));
        Optional<Double> opTelY = file.parseDouble(new ConfigurationNode(META_LOCATION_TELEPORT_Y));
        Optional<Double> opTelZ = file.parseDouble(new ConfigurationNode(META_LOCATION_TELEPORT_Z));
        Optional<String> opTelWorld = file.parseString(new ConfigurationNode(META_LOCATION_TELEPORT_WORLD));
        if (opTelWorld.isPresent() && opTelX.isPresent() && opTelY.isPresent() && opTelZ.isPresent()) {
            CorePlugin.getServer().getWorldByPlatformSpecific(opTelWorld.get()).ifPresent(w -> ship.setTeleportPosition(w.getPosition(opTelX.get(), opTelY.get(), opTelZ.get())));
        }
        new ConfigurationNode("Meta", "Location", "Teleport").getDirectChildren(file).stream().forEach(c -> {
            String[] path = c.getPath();
            String id = path[path.length - 1];
            Optional<Double> opTelX2 = file.parseDouble(new ConfigurationNode(c, "X"));
            Optional<Double> opTelY2 = file.parseDouble(new ConfigurationNode(c, "Y"));
            Optional<Double> opTelZ2 = file.parseDouble(new ConfigurationNode(c, "Z"));
            Optional<String> opTelWorldString = file.parseString(new ConfigurationNode(c, "World"));
            if (opTelWorldString.isPresent() && opTelX2.isPresent() && opTelY2.isPresent() && opTelZ2.isPresent()) {
                double pX = opTelX2.get() + opX.get();
                double pY = opTelY2.get() + opY.get();
                double pZ = opTelZ2.get() + opZ.get();
                String sWorld = opTelWorldString.get();
                CorePlugin.getServer().getWorldByPlatformSpecific(sWorld).ifPresent(w -> ((ShipsVessel) vessel).getTeleportPositions().put(id, w.getPosition(pX, pY, pZ)));
            }
        });
        CorePlugin.createSchedulerBuilder().setDisplayName(ship.getId() + " - Structure-Loader").setDelayUnit(null).setDelay(1).setExecutor(() -> file.parseList(new ConfigurationNode(META_STRUCTURE), Parser.STRING_TO_VECTOR3INT).ifPresent(structureList -> {
            new StructureLoad(ship).load(position, structureList);
        })).build(ShipsPlugin.getPlugin()).run();

        ShipsPlugin.getPlugin()
                .getDefaultPermissions()
                .forEach(p -> file.parseList(new ConfigurationNode("Meta","Permission", p.getId()), Parser.STRING_TO_UNIQUIE_ID)
                        .ifPresent(list -> list.forEach(u -> ship.getCrew().put(u, p))));
        file.parseList(new ConfigurationNode(META_FLAGS),ShipsParsers.STRING_TO_VESSEL_FLAG)
                .ifPresent(flags ->flags.forEach(ship::set));
        try {
            ship.deserializeExtra(file);
        }catch(Throwable e) {
            throw new WrappedFileLoadVesselException(this.file, e);
        }
        return ship;
    }

    public static File getVesselDataFolder() {
        return new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "VesselData");
    }

    public static Set<ShipsVessel> loadAll(Consumer<LoadVesselException> function) {
        Set<ShipsVessel> set = new HashSet<>();
        ShipsPlugin.getPlugin().getAll(ShipType.class).forEach(st -> {
            File vesselDataFolder = getVesselDataFolder();
            File typeFolder = new File(vesselDataFolder, st.getId().replaceAll(":", "."));
            File[] files = typeFolder.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                try {
                    ShipsVessel vessel = new ShipsFileLoader(file).load();
                    if (vessel == null) {
                        continue;
                    }
                    set.add(vessel);
                } catch (LoadVesselException e) {
                    function.accept(e);
                }
            }
        });
        return set;
    }
}
