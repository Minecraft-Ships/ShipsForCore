package org.ships.plugin;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.command.CommandRegister;
import org.core.logger.Logger;
import org.core.platform.plugin.CorePlugin;
import org.core.platform.plugin.details.CorePluginVersion;
import org.core.platform.update.PluginUpdate;
import org.core.platform.update.bukkit.DevBukkitUpdateChecker;
import org.core.platform.update.bukkit.DevBukkitUpdateOption;
import org.core.platform.update.result.FailedResult;
import org.core.platform.update.result.SuccessfulResult;
import org.core.schedule.Scheduler;
import org.core.source.command.ConsoleSource;
import org.core.utils.Identifiable;
import org.core.world.structure.StructureFileBuilder;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.blockfinder.BasicBlockFinder;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.commands.argument.ShipsArgumentCommand;
import org.ships.config.blocks.DefaultBlockList;
import org.ships.config.configuration.LegacyShipsConfig;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.debug.DebugFile;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.event.listener.CoreEventListener;
import org.ships.exceptions.load.FileLoadVesselException;
import org.ships.movement.BlockPriority;
import org.ships.movement.PreventMovementManager;
import org.ships.movement.autopilot.path.FlightPathManager;
import org.ships.movement.autopilot.scheduler.FallExecutor;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.flag.PlayerStatesFlag;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.loader.shipsvessel.ShipsFileLoader;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipType;
import org.ships.vessel.converts.vessel.VesselConverter;
import org.ships.vessel.converts.vessel.shipsfive.Ships5VesselConverter;
import org.ships.vessel.sign.*;
import org.ships.vessel.sign.lock.LockedSignManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShipsPlugin implements CorePlugin {

    public static final double PRERELEASE_VERSION = 15.7;
    public static final String PRERELEASE_TAG = "Beta";
    private static ShipsPlugin plugin;
    private final Map<String, VesselFlag.Builder<?, ?>> vesselFlags = new HashMap<>();
    private final Collection<Identifiable> identifiables = new HashSet<>();
    private final LockedSignManager lockedSignManager = new LockedSignManager();
    private final Collection<Vessel> vessels = new LinkedHashSet<>();
    private final FlightPathManager flightPaths = new FlightPathManager();
    private final PreventMovementManager preventMovement = new PreventMovementManager();
    private Logger logger;
    private DefaultBlockList blockList;
    private AdventureMessageConfig aMessageConfig;
    private ShipsConfig config;
    private DebugFile debugFile;
    private Object launcher;
    private boolean shutdown;

    public ShipsPlugin() {
        plugin = this;
    }

    public @NotNull LockedSignManager getLockedSignManager() {
        return this.lockedSignManager;
    }

    public boolean isShuttingDown() {
        return this.shutdown;
    }

    public @NotNull FlightPathManager getFlightPathManager() {
        return this.flightPaths;
    }

    public PreventMovementManager getPreventMovementManager() {
        return this.preventMovement;
    }

    public void loadStructures() {
        File file = new File(this.getConfigFolder(), "Structure");
        File[] pluginFolders = file.listFiles();
        if (pluginFolders == null) {
            return;
        }
        for (File pluginFolder : pluginFolders) {
            File[] structureFiles = pluginFolder.listFiles();
            if (structureFiles == null) {
                continue;
            }
            for (File structureFile : structureFiles) {
                String structureName = structureFile.getName();
                if (structureName.endsWith(".structure")) {
                    structureName = structureName.substring(0, structureName.length() - 10);
                }
                StructureFileBuilder fileBuilder = new StructureFileBuilder()
                        .setFile(structureFile)
                        .setPlugin(ShipsPlugin.getPlugin())
                        .setKey(structureName.replaceAll(" ", "_").toLowerCase())
                        .setName(structureName);
                try {
                    TranslateCore.getPlatform().register(fileBuilder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public AdventureMessageConfig getAdventureMessageConfig() {
        return this.aMessageConfig;
    }

    public void initShipType() {
        for (ShipType<?> type : this.getAllShipTypes()) {
            if (!(type instanceof AbstractShipType)) {
                continue;
            }
            ((AbstractShipType<?>) type).init();
        }
    }

    public void loadCustomShipType() {
        File folder = new File(this.getConfigFolder(), "Configuration/ShipType/Custom");
        for (CloneableShipType<?> type : this.getAllCloneableShipTypes()) {
            File folderType = new File(folder, type.getId().replace(":", ".") + "/");
            File[] files = folderType.listFiles();
            if (files == null) {
                if (!folderType.exists() && !folderType.mkdirs()) {
                    System.err.println("Could not create folder at '" + folderType.getPath() + "'");
                }
                continue;
            }
            for (File file : files) {
                this.identifiables.add(type.cloneWithName(file));
            }
        }
    }

    public void loadVessels() {
        ShipsConfig config = this.getConfig();
        this.vessels.addAll(ShipsFileLoader.loadAll((e) -> {
            if (e instanceof FileLoadVesselException flve && config.willDeleteFilesIfFailedToLoad()) {
                flve.getFile().delete();
            }
            e.printStackTrace();
        }));
    }

    public void loadConverts() {
        this.getAll(VesselConverter.class).forEach(c -> {
            File folder = c.getFolder();
            File[] files = folder.listFiles();
            if (files == null) {
                return;
            }
            Stream.of(files).filter(f -> !f.isDirectory()).forEach(f -> {
                try {
                    this.registerVessel(c.convert(f));
                } catch (IOException e) {
                    System.err.println("Error converting vessel with " + c.getId() + " at: " + f.getPath());
                    e.printStackTrace();
                }
            });
        });
    }

    private void init() {
        this.identifiables.add(BasicMovement.SHIPS_FIVE);
        this.identifiables.add(BasicMovement.SHIPS_SIX);

        this.identifiables.add(BasicBlockFinder.SHIPS_FIVE);
        this.identifiables.add(BasicBlockFinder.SHIPS_FIVE_ASYNC);
        this.identifiables.add(BasicBlockFinder.SHIPS_SIX);
        this.identifiables.add(BasicBlockFinder.SHIPS_SIX_RELEASE_ONE_MULTI_ASYNC);
        this.identifiables.add(BasicBlockFinder.SHIPS_SIX_RELEASE_ONE_SINGLE_ASYNC);
        this.identifiables.add(BlockPriority.AIR);
        this.identifiables.add(BlockPriority.DIRECTIONAL);
        this.identifiables.add(BlockPriority.ATTACHED);
        this.identifiables.add(BlockPriority.NORMAL);
        this.identifiables.add(new Ships5VesselConverter());
        this.identifiables.add(new LicenceSign());
        this.identifiables.add(new AltitudeSign());
        this.identifiables.add(new WheelSign());
        this.identifiables.add(new MoveSign());
        this.identifiables.add(new EOTSign());
        this.register(CrewPermission.CAPTAIN, CrewPermission.CREW_MEMBER, CrewPermission.DEFAULT);
        this.vesselFlags.put("ships:player_states", new PlayerStatesFlag.Builder());
    }

    private void init2() {
        this.identifiables.add(ShipType.OVERPOWERED_SHIP);
        this.identifiables.add(ShipType.AIRSHIP);
        this.identifiables.add(ShipType.WATERSHIP);
        this.identifiables.add(ShipType.SUBMARINE);
        this.identifiables.add(ShipType.MARSSHIP);
        this.identifiables.add(ShipType.PLANE);
    }

    public ShipsConfig getConfig() {
        return this.config;
    }

    public void getLoadedMessages() {
        ConsoleSource source = TranslateCore.getConsole();
        source.sendMessage(
                AText.ofPlain("------[Ships Loaded Information][Start]------").withColour(NamedTextColours.RED));
        source.sendMessage(AText
                                   .ofPlain("Ships Version: ")
                                   .withColour(NamedTextColours.AQUA)
                                   .append(AText.ofPlain(this.getPluginVersion().asString() + ":" + PRERELEASE_TAG + "-"
                                                                 + PRERELEASE_VERSION)));
        source.sendMessage(AText
                                   .ofPlain("Vessels: ")
                                   .withColour(NamedTextColours.AQUA)
                                   .append(AText
                                                   .ofPlain("" + this.vessels.size())
                                                   .withColour(NamedTextColours.YELLOW)));
        source.sendMessage(
                AText.ofPlain("------[Ships Loaded Information][End]------").withColour(NamedTextColours.RED));
    }

    public void loadVesselTypeFlagData() {
        this.getAll(AbstractShipType.class).forEach(vt -> {
            vt.initFlags();
            vt.save();
        });
    }

    public DebugFile getDebugFile() {
        return this.debugFile;
    }

    public DefaultBlockList getBlockList() {
        return this.blockList;
    }

    public Collection<Vessel> getVessels() {
        return this.vessels;
    }

    public <T extends Identifiable> Collection<T> getAll(Class<T> class1) {
        return this.identifiables
                .parallelStream()
                .filter(class1::isInstance)
                .map(t -> (T) t)
                .collect(Collectors.toSet());
    }

    public Collection<ShipType<?>> getAllShipTypes() {
        return (Collection<ShipType<?>>) (Object) this.getAll(ShipType.class);
    }

    public @NotNull Collection<CloneableShipType<?>> getAllCloneableShipTypes() {
        return (Collection<CloneableShipType<?>>) (Object) this.getAll(CloneableShipType.class);
    }

    public <T extends Identifiable> Optional<T> get(@NotNull Class<T> class1) {
        return this.identifiables.stream().filter(class1::isInstance).map(t -> (T) t).findAny();
    }

    public @NotNull Map<String, VesselFlag.Builder<?, ?>> getVesselFlags() {
        return this.vesselFlags;
    }

    public void registerVessel(@NotNull Vessel vessel) {
        this.vessels.add(vessel);
    }

    public void unregisterVessel(@NotNull Vessel vessel) {
        this.vessels.remove(vessel);
    }

    @Deprecated
    public void register() {
        throw new RuntimeException("Must specify a Identifiable to register");
    }

    public void register(Identifiable... identifiables) {
        this.identifiables.addAll(Arrays.asList(identifiables));
    }

    @Deprecated
    public void unregister() {
        throw new RuntimeException("Must specify an Identifiable to unregister");
    }

    public void unregister(Identifiable... identifiables) {
        Arrays.asList(identifiables).forEach(this.identifiables::remove);
    }

    public void register(@NotNull String id, @NotNull VesselFlag.Builder<?, ?> flag) {
        this.vesselFlags.put(id, flag);
    }

    @Override
    public @NotNull String getPluginName() {
        return "Ships";
    }

    @Override
    public @NotNull String getPluginId() {
        return "ships";
    }

    @Override
    public void onCoreReady() {
        this.init();
        LegacyShipsConfig legacyShipsConfig = new LegacyShipsConfig();
        this.config = legacyShipsConfig.isLegacy() ? legacyShipsConfig.convertToNew() : new ShipsConfig();
        this.aMessageConfig = new AdventureMessageConfig();
        this.blockList = new DefaultBlockList();
        this.debugFile = new DebugFile();
        TranslateCore.getEventManager().register(this, new CoreEventListener());
        if (this.config.isFallingEnabled()) {
            Scheduler fallScheduler = FallExecutor.createScheduler();
            fallScheduler.run();
        }
        this.init2();
    }

    @Override
    public void onCoreFinishedInit() {
        this.loadCustomShipType();
        this.initShipType();
        this.loadVesselTypeFlagData();
        this.loadVessels();
        this.getLoadedMessages();

        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        if (config.isUpdateEnabled()) {
            TranslateCore.getPlatform().getUpdateChecker(DevBukkitUpdateChecker.ID).ifPresent(devBukkit -> {
                devBukkit.checkForUpdate(new DevBukkitUpdateOption(36846)).thenAcceptAsync((result) -> {
                    if (result instanceof FailedResult failed) {
                        this.logger.error("Failed to update: " + failed.getReason());
                        return;
                    }
                    SuccessfulResult successfulResult = (SuccessfulResult) result;
                    PluginUpdate context = successfulResult.getUpdate();
                    String fullVersionName = context.getName();
                    String currentVersionName =
                            "Ships -B " + this.getPluginVersion().asString() + ".0 R2 " + PRERELEASE_TAG + " "
                                    + PRERELEASE_VERSION;
                    if (fullVersionName.equals(currentVersionName)) {
                        return;
                    }
                    this.logger.log("An update can be downloaded");
                    this.logger.log("\tCurrent Version: " + currentVersionName);
                    this.logger.log("\tUpdated Version: " + fullVersionName);
                    this.logger.log("\tFor Minecraft: " + context.getVersion());
                    this.logger.log("\tDownload At: " + context.getDownloadURL().toExternalForm());
                });
            });
        }
    }

    @Override
    public @NotNull Object getPlatformLauncher() {
        return this.launcher;
    }

    @Override
    public void onConstruct(@NotNull Object pluginLauncher, @NotNull Logger logger) {
        this.launcher = pluginLauncher;
        this.logger = logger;
        File file = new File(this.getConfigFolder().getParentFile(), "Ships");
        if (file.exists()) {
            file.renameTo(this.getConfigFolder());
        }
    }

    @Override
    public void onRegisterCommands(@NotNull CommandRegister register) {
        register.register(new ShipsArgumentCommand());
    }

    @Override
    public @NotNull String getLicence() {
        return "All Rights Reserved";
    }

    @Override
    public @NotNull CorePluginVersion getPluginVersion() {
        return new CorePluginVersion(6, 0, 0);
    }

    @Override
    public void onShutdown() {
        shutdown = true;
    }

    public @NotNull Logger getLogger() {
        return this.logger;
    }

    public static @NotNull ShipsPlugin getPlugin() {
        if (plugin == null) {
            throw new RuntimeException("Ships has not loaded");
        }
        return plugin;
    }
}
