package org.ships.plugin;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.command.CommandRegister;
import org.core.platform.plugin.CorePlugin;
import org.core.platform.plugin.details.CorePluginVersion;
import org.core.schedule.Scheduler;
import org.core.schedule.unit.TimeUnit;
import org.core.source.command.ConsoleSource;
import org.core.utils.Identifiable;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.blockfinder.BasicBlockFinder;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.commands.argument.ShipsArgumentCommand;
import org.ships.config.blocks.DefaultBlockList;
import org.ships.config.configuration.LegacyShipsConfig;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.debug.DebugFile;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.config.messages.MessageConfig;
import org.ships.event.listener.CoreEventListener;
import org.ships.movement.BlockPriority;
import org.ships.movement.autopilot.scheduler.FallExecutor;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.patches.AutoRunPatches;
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

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShipsPlugin implements CorePlugin {

    public static final double PRERELEASE_VERSION = 14.0;
    public static final String PRERELEASE_TAG = "Beta";
    private static ShipsPlugin plugin;
    private final Map<String, VesselFlag.Builder<?, ?>> vesselFlags = new HashMap<>();
    private final Set<Identifiable> identifiables = new HashSet<>();
    private final Set<Vessel> vessels = new HashSet<>();
    private DefaultBlockList blockList;
    @Deprecated
    private
    MessageConfig messageConfig;
    private AdventureMessageConfig aMessageConfig;
    private ShipsConfig config;
    private DebugFile debugFile;
    private Object launcher;

    public ShipsPlugin() {
        plugin = this;
    }

    public static ShipsPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void onCoreReady() {
        this.init();
        LegacyShipsConfig legacyShipsConfig = new LegacyShipsConfig();
        this.config = legacyShipsConfig.isLegacy() ? legacyShipsConfig.convertToNew():new ShipsConfig();
        this.messageConfig = new MessageConfig();
        this.aMessageConfig = new AdventureMessageConfig();
        this.blockList = new DefaultBlockList();
        this.debugFile = new DebugFile();
        TranslateCore.getEventManager().register(this, new CoreEventListener());
        if (this.config.isFallingEnabled()) {
            Scheduler fallScheduler = FallExecutor.createScheduler();
            fallScheduler.run();
        }
        TranslateCore
                .createSchedulerBuilder()
                .setDisplayName("Ships no gravity fix")
                .setIteration(1)
                .setIterationUnit(TimeUnit.SECONDS)
                .setExecutor(AutoRunPatches.NO_GRAVITY_FIX)
                .build(this)
                .run();
        this.init2();
    }

    @Override
    public @NotNull Object getPlatformLauncher() {
        return this.launcher;
    }

    @Override
    public void onRegisterCommands(@NotNull CommandRegister register) {
        register.register(new ShipsArgumentCommand());
    }

    @Override
    public void onCoreFinishedInit() {
        this.loadCustomShipType();
        this.loadVesselTypeFlagData();
        this.loadVessels();
        this.getLoadedMessages();

        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        if (config.isUpdateEnabled()) {
            System.err.println("Updating has been disabled");
        }
    }

    @Deprecated
    public MessageConfig getMessageConfig() {
        return this.messageConfig;
    }

    public AdventureMessageConfig getAdventureMessageConfig() {
        return this.aMessageConfig;
    }

    public void loadCustomShipType() {
        File folder = new File(this.getConfigFolder(), "Configuration/ShipType/Custom");
        for (CloneableShipType<?> type : this.getAllCloneableShipTypes()) {
            File folderType = new File(folder, type.getId().replace(":", ".") + "/");
            File[] files = folderType.listFiles();
            if (files==null) {
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
        this.vessels.addAll(ShipsFileLoader.loadAll(Throwable::printStackTrace));
    }

    public void loadConverts() {
        this.getAll(VesselConverter.class).forEach(c -> {
            File folder = c.getFolder();
            File[] files = folder.listFiles();
            if (files==null) {
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
        this.identifiables.add(BasicMovement.SHIPS_FIVE_ASYNC);

        this.identifiables.add(BasicBlockFinder.SHIPS_FIVE);
        this.identifiables.add(BasicBlockFinder.SHIPS_FIVE_ASYNC);
        this.identifiables.add(BasicBlockFinder.SHIPS_SIX);
        this.identifiables.add(BasicBlockFinder.SHIPS_SIX_RELEASE_ONE_ASYNC);
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
        source.sendMessage(AText.ofPlain("------[Ships Loaded Information][Start]------").withColour(NamedTextColours.RED));
        this.displayMessage(BasicBlockFinder.class, "BlockFinders", bf -> "");
        this.displayMessage(BasicMovement.class, "MovementMethods", bm -> "");
        this.displayMessage(BlockPriority.class, "BlockPriorities", bp -> bp.getPriorityNumber() + "");
        this.displayMessage(ShipsSign.class, "Signs", sn -> "");
        this.displayMessage(ShipType.class, "ShipTypes", st -> st.getDisplayName() + (st.getDisplayName().length() > 7 ? "\t":"\t\t") + st.getFile().getFile().getPath());
        source.sendMessage(AText.ofPlain("Vessels: ").withColour(NamedTextColours.AQUA).append(AText.ofPlain("" + this.vessels.size()).withColour(NamedTextColours.YELLOW)));
        source.sendMessage(AText.ofPlain("------[Ships Loaded Information][End]------").withColour(NamedTextColours.RED));
    }

    public void loadVesselTypeFlagData() {
        this.getAll(AbstractShipType.class).forEach(vt -> {
            vt.initFlags();
            vt.save();
        });
    }

    private <I extends Identifiable> void displayMessage(Class<I> class1, String name, Function<? super I, String> function) {
        Set<I> values = this.getAll(class1);
        ConsoleSource source = TranslateCore.getConsole();
        source.sendMessage(AText.ofPlain("Found " + name + ": " + values.size()).withColour(NamedTextColours.AQUA));
        values.forEach(v -> {
            String id = v.getId();
            String text = function.apply(v);
            AText ret = AText.ofPlain("\t- " + id + (id.length() > 13 ? "\t":"\t\t") + text).withColour(NamedTextColours.YELLOW);
            source.sendMessage(ret);
        });
    }

    public DebugFile getDebugFile() {
        return this.debugFile;
    }

    public DefaultBlockList getBlockList() {
        return this.blockList;
    }

    @Deprecated
    public Set<CrewPermission> getDefaultPermissions() {
        return this.getAll(CrewPermission.class);
    }

    public Set<Vessel> getVessels() {
        return this.vessels;
    }

    public <T extends Identifiable> Set<T> getAll(Class<T> class1) {
        return this.identifiables.stream().filter(class1::isInstance).map(t -> (T) t).collect(Collectors.toSet());
    }

    public Set<ShipType<?>> getAllShipTypes() {
        return (Set<ShipType<?>>) (Object) this.getAll(ShipType.class);
    }

    public Set<CloneableShipType<?>> getAllCloneableShipTypes() {
        return (Set<CloneableShipType<?>>) (Object) this.getAll(CloneableShipType.class);
    }

    public <T extends Identifiable> Optional<T> get(Class<T> class1) {
        return this.identifiables.stream().filter(class1::isInstance).map(t -> (T) t).findAny();
    }

    public Map<String, VesselFlag.Builder<?, ?>> getVesselFlags() {
        return this.vesselFlags;
    }

    public void registerVessel(@NotNull Vessel vessel) {
        this.vessels.add(vessel);
    }

    public void unregisterVessel(@NotNull Vessel vessel) {
        this.vessels.remove(vessel);
    }

    public void register(Identifiable... identifiables) {
        this.identifiables.addAll(Arrays.asList(identifiables));
    }

    public void unregister(Identifiable... identifiables) {
        Arrays.asList(identifiables).forEach(this.identifiables::remove);
    }

    /*public void register(CrewPermission... permissions) {
        this.defaultPermissions.addAll(Arrays.asList(permissions));
    }*/

    public void register(String id, VesselFlag.Builder<?, ?> flag) {
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
    public void onConstruct(@NotNull Object pluginLauncher) {
        this.launcher = pluginLauncher;
        File file = new File(this.getConfigFolder().getParentFile(), "Ships");
        if (file.exists()) {
            file.renameTo(this.getConfigFolder());
        }
    }

    @Override
    public @NotNull CorePluginVersion getPluginVersion() {
        return new CorePluginVersion(6, 0, 0);
    }

    @Override
    public @NotNull String getLicence() {
        return "All Rights Reserved";
    }
}
