package org.ships.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.core.TranslateCore;
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
import org.core.world.structure.StructureFileBuilder;
import org.jetbrains.annotations.NotNull;
import org.ships.commands.argument.ShipsArgumentCommand;
import org.ships.config.blocks.DefaultBlockList;
import org.ships.config.configuration.LegacyShipsConfig;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.debug.DebugFile;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.event.listener.CoreEventListener;
import org.ships.exceptions.load.FileLoadVesselException;
import org.ships.movement.PreventMovementManager;
import org.ships.movement.autopilot.path.FlightPathManager;
import org.ships.movement.autopilot.scheduler.FallExecutor;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.flag.PlayerStatesFlag;
import org.ships.vessel.common.flag.VesselFlags;
import org.ships.vessel.common.loader.shipsvessel.ShipsFileLoader;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipTypes;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipType;
import org.ships.vessel.converts.ShipsConverters;
import org.ships.vessel.converts.vessel.VesselConverter;
import org.ships.vessel.sign.lock.LockedSignManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShipsPlugin implements CorePlugin {

    public static final double PRERELEASE_VERSION = 17;
    public static final String PRERELEASE_TAG = "Beta";
    private static ShipsPlugin plugin;
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
        for (ShipType<?> type : ShipTypes.shipTypes()) {
            if (!(type instanceof AbstractShipType)) {
                continue;
            }
            ((AbstractShipType<?>) type).init();
        }
    }

    public void loadCustomShipType() {
        File folder = new File(this.getConfigFolder(), "Configuration/ShipType/Custom");
        for (CloneableShipType<?> type : ShipTypes.cloneableShipTypes().collect(Collectors.toList())) {
            File folderType = new File(folder, type.getId().replace(":", ".") + "/");
            File[] files = folderType.listFiles();
            if (files == null) {
                if (!folderType.exists() && !folderType.mkdirs()) {
                    System.err.println("Could not create folder at '" + folderType.getPath() + "'");
                }
                continue;
            }
            for (File file : files) {
                ShipTypes.registerType(type.cloneWithName(file));
            }
        }
    }

    public void loadVessels() {
        ShipsConfig config = this.getConfig();
        this.vessels.addAll(ShipsFileLoader.loadAll((e) -> {
            if (e instanceof FileLoadVesselException && config.willDeleteFilesIfFailedToLoad()) {
                FileLoadVesselException flve = (FileLoadVesselException) e;
                flve.getFile().delete();
            }
            e.printStackTrace();
        }));
    }

    public void loadConverts() {
        ShipsConverters.converters().forEach(c -> {
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
        VesselFlags.registerBuilder("ships:player_states", new PlayerStatesFlag.Builder());
    }

    public ShipsConfig getConfig() {
        return this.config;
    }

    public void getLoadedMessages() {
        ConsoleSource source = TranslateCore.getConsole();
        source.sendMessage(Component.text("------[Ships Loaded Information][Start]------").color(NamedTextColor.RED));
        source.sendMessage(Component
                                   .text("Ships Version: ")
                                   .color(NamedTextColor.AQUA)
                                   .append(Component.text(
                                           this.getPluginVersion().asString() + ":" + PRERELEASE_TAG + "-"
                                                   + PRERELEASE_VERSION)));
        source.sendMessage(Component
                                   .text("Vessels: ")
                                   .color(NamedTextColor.AQUA)
                                   .append(Component.text("" + this.vessels.size()).color(NamedTextColor.YELLOW)));
        source.sendMessage(Component.text("------[Ships Loaded Information][End]------").color(NamedTextColor.RED));
    }

    public void loadVesselTypeFlagData() {
        ShipTypes
                .shipTypes()
                .stream()
                .filter(t -> t instanceof AbstractShipType)
                .map(t -> (AbstractShipType<?>) t)
                .forEach(vt -> {
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

    public void registerVessel(@NotNull Vessel vessel) {
        this.vessels.add(vessel);
    }

    public void unregisterVessel(@NotNull Vessel vessel) {
        this.vessels.remove(vessel);
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
            TranslateCore
                    .getPlatform()
                    .getUpdateChecker(DevBukkitUpdateChecker.ID)
                    .ifPresent(devBukkit -> devBukkit
                            .checkForUpdate(new DevBukkitUpdateOption(36846))
                            .thenAcceptAsync((result) -> {
                                if (result instanceof FailedResult) {
                                    FailedResult failed = (FailedResult) result;
                                    this.logger.error("Failed to update: " + failed.getReason());
                                    return;
                                }
                                SuccessfulResult successfulResult = (SuccessfulResult) result;
                                PluginUpdate context = successfulResult.getUpdate();
                                String fullVersionName = context.getName();
                                String currentVersionName =
                                        "Ships -" + TranslateCore.getPlatform().getImplementationDetails().getTagChar()
                                                + " " + this.getPluginVersion().asString() + ".0 R2 " + PRERELEASE_TAG
                                                + " " + PRERELEASE_VERSION;
                                if (fullVersionName.equals(currentVersionName)) {
                                    return;
                                }
                                ConsoleSource console = TranslateCore.getConsole();
                                console.sendMessage(Component
                                                            .text("An update can be downloaded for Ships")
                                                            .color(NamedTextColor.GREEN));
                                console.sendMessage(Component
                                                            .text("Current Version: ")
                                                            .append(Component
                                                                            .text(currentVersionName)
                                                                            .color(TextColor.color(255, 100, 100))));
                                console.sendMessage(Component
                                                            .text("Updated Version: ")
                                                            .append(Component
                                                                            .text(fullVersionName)
                                                                            .color(TextColor.color(100, 255, 100))
                                                                            .append(Component
                                                                                            .text(": For Minecraft: ")
                                                                                            .append(Component.text(
                                                                                                    context.getVersion())))));
                                console.sendMessage(Component
                                                            .text("Download At: ")
                                                            .append(Component
                                                                            .text(context
                                                                                          .getDownloadURL()
                                                                                          .toExternalForm())
                                                                            .clickEvent(ClickEvent.openUrl(context
                                                                                                                   .getDownloadURL()
                                                                                                                   .toExternalForm()))));
                            }));
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
        this.shutdown = true;
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
