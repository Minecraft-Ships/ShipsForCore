package org.ships.vessel.common.loader.shipsvessel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.array.utils.ArrayUtils;
import org.core.TranslateCore;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.schedule.unit.TimeUnit;
import org.core.utils.ComponentUtils;
import org.core.utils.Else;
import org.core.vector.type.Vector3;
import org.core.world.WorldExtent;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.config.parsers.ShipsParsers;
import org.ships.config.parsers.VesselFlagWrappedParser;
import org.ships.exceptions.NoLicencePresent;
import org.ships.exceptions.load.FileLoadVesselException;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.WrappedFileLoadVesselException;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.permissions.vessel.CrewPermissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.TeleportToVessel;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.flag.VesselFlags;
import org.ships.vessel.common.loader.ShipsLoader;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipTypes;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.sign.ShipsSigns;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShipsFileLoader implements ShipsLoader {

    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> SIZE_MAX = new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_INTEGER, "Block", "Count", "Max");
    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> SIZE_MIN = new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_INTEGER, "Speed", "Count", "Min");

    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> SPEED_MAX = new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_INTEGER, "Speed", "Max");
    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> SPEED_ALTITUDE = new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_INTEGER, "Speed", "Altitude");
    public static final ConfigurationNode.KnownParser.SingleKnown<CrewPermission> META_DEFAULT_PERMISSION = new ConfigurationNode.KnownParser.SingleKnown<>(
            ShipsParsers.STRING_TO_CREW_PERMISSION, "Meta", "Permission", "Default");
    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> META_LOCATION_X = new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_INTEGER, "Meta", "Location", "X");
    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> META_LOCATION_Y = new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_INTEGER, "Meta", "Location", "Y");
    public static final ConfigurationNode.KnownParser.SingleKnown<Integer> META_LOCATION_Z = new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_INTEGER, "Meta", "Location", "Z");
    public static final ConfigurationNode.KnownParser.SingleKnown<WorldExtent> META_LOCATION_WORLD = new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_WORLD, "Meta", "Location", "World");
    public static final ConfigurationNode.KnownParser.CollectionKnown<Vector3<Integer>> META_STRUCTURE = new ConfigurationNode.KnownParser.CollectionKnown<>(
            Parser.STRING_TO_VECTOR3INT, "Meta", "Location", "Structure");
    public static final ConfigurationNode.GroupKnown<VesselFlag<?>> META_FLAGS = new ConfigurationNode.GroupKnown<>(
            () -> VesselFlags
                    .builders()
                    .entrySet()
                    .stream()
                    .collect(
                            Collectors.<Map.Entry<String, VesselFlag.Builder<?, ?>>, String, Parser<String, VesselFlag<?>>>toMap(
                                    e -> e.getKey().replaceAll(":", " "),
                                    new Function<Map.Entry<String, VesselFlag.Builder<?, ?>>, VesselFlagWrappedParser<?>>() {


                                        @Override
                                        public VesselFlagWrappedParser<?> apply(Map.Entry<String, VesselFlag.Builder<?, ?>> stringVesselFlagEntry) {
                                            return this.build(stringVesselFlagEntry.getValue());
                                        }

                                        private <T> VesselFlagWrappedParser<?> build(VesselFlag.Builder<?, ?> builder) {
                                            return new VesselFlagWrappedParser<>(
                                                    (VesselFlag.Builder<T, VesselFlag<T>>) builder);
                                        }
                                    })), i -> i.getId().replaceAll(":", " "), "Meta", "Flags");

    private final static class StructureLoad {

        private final ShipsVessel ship;

        private StructureLoad(ShipsVessel shipsVessel) {
            this.ship = shipsVessel;
        }

        public CompletableFuture<PositionableShipsStructure> load(BlockPosition position,
                                                                  Collection<? extends Vector3<Integer>> structureList) {

            if (structureList.isEmpty()) {
                return ship.updateStructure().thenApply(structure -> {
                    TranslateCore
                            .getConsole()
                            .sendMessage(Component.text(
                                    Else.throwOr(NoLicencePresent.class, StructureLoad.this.ship::getId, "Unknown")
                                            + " has loaded."));
                    return structure;
                });
            }
            PositionableShipsStructure pss = this.ship.getStructure();
            pss.setRaw(structureList);
            this.ship.setLoading(false);
            TranslateCore
                    .getConsole()
                    .sendMessage(Component.text(
                            Else.throwOr(NoLicencePresent.class, this.ship::getId, "") + " has loaded."));
            return CompletableFuture.completedFuture(pss);

        }
    }

    protected final File file;

    public ShipsFileLoader(File file) {
        this.file = file;
    }

    public void save(AbstractShipsVessel vessel) {
        try {
            saveToFile(vessel);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void saveToFile(AbstractShipsVessel vessel) {
        ConfigurationStream.ConfigurationFile file = TranslateCore.createConfigurationFile(this.file, TranslateCore
                .getPlatform()
                .getConfigFormat());
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

        vessel.serialize(file).forEach((key, value) -> {
            if (key instanceof ConfigurationNode.KnownParser.SingleKnown) {
                this.setSingleInFile(file, (ConfigurationNode.KnownParser.SingleKnown<?>) key, value);
            } else if (key instanceof ConfigurationNode.KnownParser.CollectionKnown) {
                this.setCollectionInFile(file, (ConfigurationNode.KnownParser.CollectionKnown<?>) key,
                                         (Collection<?>) value);
            } else {
                throw new IllegalArgumentException("Could not understand what to do with " + key.getClass().getName());
            }
        });
        file.set(SPEED_MAX, vessel.getMaxSpeed());
        file.set(SPEED_ALTITUDE, vessel.getAltitudeSpeed());
        file.set(META_LOCATION_WORLD, vessel.getPosition().getWorld());
        file.set(META_LOCATION_X, vessel.getPosition().getX());
        file.set(META_LOCATION_Y, vessel.getPosition().getY());
        file.set(META_LOCATION_Z, vessel.getPosition().getZ());
        vessel.getMaxSize().ifPresent((size) -> file.set(SIZE_MAX, size));
        //TODO file.set(SIZE_MIN, vessel.getMinSize());
        vessel.getTeleportVectors().forEach((key, value) -> {
            file.set(new ConfigurationNode("Meta", "Location", "Teleport", key, "X"), value.getX());
            file.set(new ConfigurationNode("Meta", "Location", "Teleport", key, "Y"), value.getY());
            file.set(new ConfigurationNode("Meta", "Location", "Teleport", key, "Z"), value.getZ());
        });
        uuidList.forEach((key, value) -> file.set(
                new ConfigurationNode.KnownParser.CollectionKnown<>(Parser.STRING_TO_STRING_PARSER, "Meta",
                                                                    "Permission", key.getId()), value));
        file.set(META_STRUCTURE, vessel.getStructure().getRelativePositionsToCenter());
        Set<VesselFlag<?>> flags = vessel
                .getFlags()
                .stream()
                .filter(s -> s instanceof VesselFlag.Serializable)
                .collect(Collectors.toSet());
        file.set(META_FLAGS, flags);
        file.save();
    }

    @Override
    public ShipsVessel load() throws LoadVesselException {
        ConfigurationStream.ConfigurationFile file = TranslateCore.createConfigurationFile(this.file, TranslateCore
                .getPlatform()
                .getConfigFormat());
        Optional<WorldExtent> opWorld = file.parse(META_LOCATION_WORLD);
        if (opWorld.isEmpty()) {
            throw new FileLoadVesselException(this.file, "Unknown World of " + file
                    .getString(META_LOCATION_WORLD)
                    .orElse("'No value found'"));
        }
        Optional<Integer> opX = file.getInteger(META_LOCATION_X);
        if (opX.isEmpty()) {
            throw new FileLoadVesselException(this.file, "Unknown X value");
        }
        Optional<Integer> opY = file.getInteger(META_LOCATION_Y);
        if (opY.isEmpty()) {
            throw new FileLoadVesselException(this.file, "Unknown Y value");
        }
        Optional<Integer> opZ = file.getInteger(META_LOCATION_Z);
        if (opZ.isEmpty()) {
            throw new FileLoadVesselException(this.file, "Unknown Z value");
        }
        SyncBlockPosition position = opWorld.get().getPosition(opX.get(), opY.get(), opZ.get());
        LicenceSign sign = ShipsSigns.LICENCE;
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if (!(opTile.isPresent() && opTile.get() instanceof LiveSignTileEntity)) {
            throw new FileLoadVesselException(this.file, "LicenceSign is not at location " + position.getX() + ","
                    + position.getY() + "," + position.getZ() + "," + position.getWorld().getName() + ": Error V1");
        }
        LiveSignTileEntity lste = (LiveSignTileEntity) opTile.get();
        if (!sign.isSign(lste)) {
            throw new FileLoadVesselException(this.file, "LicenceSign is not at location " + position.getX() + ","
                    + position.getY() + "," + position.getZ() + "," + position.getWorld().getName() + ": Error V2");
        }

        var signSide = sign
                .getSide(lste)
                .orElseThrow(() -> new FileLoadVesselException(this.file,
                                                               "LicenceSign is not at location " + position.getX() + ","
                                                                       + position.getY() + "," + position.getZ() + ","
                                                                       + position.getWorld().getName() + ": Error V4"));

        Optional<Component> opShipTypeS = signSide.getLineAt(1);
        if (opShipTypeS.isEmpty()) {
            throw new FileLoadVesselException(this.file, "LicenceSign is not at location " + position.getX() + ","
                    + position.getY() + "," + position.getZ() + "," + position.getWorld().getName() + ": Error V3");
        }
        String shipTypeS = ComponentUtils.toPlain(opShipTypeS.get());
        Collection<ShipType<?>> types = ShipTypes.shipTypes();
        Optional<ShipType<?>> opShipType = types
                .parallelStream()
                .filter(s -> s.getDisplayName().equalsIgnoreCase(shipTypeS))
                .findAny();
        if (opShipType.isEmpty()) {
            throw new FileLoadVesselException(this.file, "Unknown ShipType");
        }
        ShipType<?> type = opShipType.get();
        Vessel vessel = type.createNewVessel(signSide, lste.getPosition());
        if (!(vessel instanceof ShipsVessel)) {
            throw new FileLoadVesselException(this.file, "ShipType requires to be ShipsVessel");
        }
        ShipsVessel ship = (ShipsVessel) vessel;

        file.getInteger(SPEED_ALTITUDE).ifPresent(ship::setAltitudeSpeed);
        file.getInteger(SPEED_MAX).ifPresent(ship::setMaxSpeed);

        //TODO file.getInteger(SIZE_MAX).ifPresent(ship::setMaxSize);
        //TODO file.getInteger(SIZE_MIN).ifPresent(ship::setMinSize);
        file.getChildren(new ConfigurationNode("Meta", "Location", "Teleport")).forEach(c -> {
            String[] path = c.getPath();
            String id = path[path.length - 1];
            Optional<Double> opTelX2 = file.getDouble(
                    new ConfigurationNode(ArrayUtils.join(String.class, c.getPath(), new String[]{"X"})));
            Optional<Double> opTelY2 = file.getDouble(
                    new ConfigurationNode(ArrayUtils.join(String.class, c.getPath(), new String[]{"Y"})));
            Optional<Double> opTelZ2 = file.getDouble(
                    new ConfigurationNode(ArrayUtils.join(String.class, c.getPath(), new String[]{"Z"})));
            if (Stream.of(opTelX2, opTelY2, opTelZ2).allMatch(Optional::isPresent)) {
                double pX = opTelX2.get();
                double pY = opTelY2.get();
                double pZ = opTelZ2.get();
                ((TeleportToVessel) vessel).setTeleportVector(Vector3.valueOf(pX, pY, pZ), id);
            }
        });
        TranslateCore
                .getScheduleManager()
                .schedule()
                .setDisplayName(Else.throwOr(NoLicencePresent.class, ship::getId, "Unknown") + " - Structure-Loader")
                .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                .setDelay(1)
                .setRunner((sch) -> new StructureLoad(ship).load(position, file.parseCollection(META_STRUCTURE,
                                                                                                new ArrayList<>())))
                .build(ShipsPlugin.getPlugin())
                .run();

        CrewPermissions
                .permissions()
                .forEach(p -> file
                        .parseCollection(new ConfigurationNode("Meta", "Permission", p.getId()),
                                         Parser.STRING_TO_UNIQUE_ID, new ArrayList<>())
                        .forEach(u -> ship.getCrew().put(u, p)));
        try {
            file.parseCollection(META_FLAGS, new HashSet<>()).forEach(ship::set);
            ship.deserializeExtra(file);
        } catch (Throwable e) {
            throw new WrappedFileLoadVesselException(this.file, e);
        }
        return ship;
    }

    private <T, C extends Collection<T>> void setCollectionInFile(ConfigurationStream stream,
                                                                  ConfigurationNode.KnownParser.CollectionKnown<T> node,
                                                                  Collection<?> value) {
        stream.set(node, (C) value);
    }

    private <T> void setSingleInFile(ConfigurationStream stream,
                                     ConfigurationNode.KnownParser.SingleKnown<T> node,
                                     Object value) {
        stream.set(node, (T) value);
    }

    public static File getVesselDataFolder() {
        return new File(ShipsPlugin.getPlugin().getConfigFolder(), "VesselData");
    }

    public static Set<ShipsVessel> loadAll(Consumer<? super LoadVesselException> function) {
        Set<ShipsVessel> set = new HashSet<>();
        Collection<ShipType<?>> types = ShipTypes.shipTypes();
        types.forEach(st -> {
            try {
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
                        TranslateCore
                                .getConsole()
                                .sendMessage(Component
                                                     .text("Failed to load " + file.getAbsolutePath() + ":")
                                                     .color(NamedTextColor.RED));
                        function.accept(e);
                    } catch (Throwable e) {
                        TranslateCore
                                .getConsole()
                                .sendMessage(Component
                                                     .text("Failed to load " + file.getAbsolutePath() + ":")
                                                     .color(NamedTextColor.RED));
                        e.printStackTrace();
                    }
                }
            } catch (Throwable e) {
                TranslateCore
                        .getConsole()
                        .sendMessage(
                                Component.text("Could not load any ships of " + st.getId()).color(NamedTextColor.RED));
                e.printStackTrace();
            }
        });
        return set;
    }
}
