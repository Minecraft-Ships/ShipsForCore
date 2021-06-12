package org.ships.plugin;

import org.core.CorePlugin;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.command.CommandRegister;
import org.core.platform.Plugin;
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
import org.ships.vessel.converts.vessel.VesselConverter;
import org.ships.vessel.converts.vessel.shipsfive.Ships5VesselConverter;
import org.ships.vessel.sign.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ShipsPlugin implements Plugin {

    public static final double PRERELEASE_VERSION = 12.1;
    public static final String PRERELEASE_TAG = "Beta";
    private static ShipsPlugin plugin;
    private final Map<String, VesselFlag.Builder<?, ?>> vesselFlags = new HashMap<>();
    private final Set<Identifiable> identifiables = new HashSet<>();
    private final Set<Vessel> vessels = new HashSet<>();
    private DefaultBlockList blockList;
    private @Deprecated
    MessageConfig messageConfig;
    private AdventureMessageConfig aMessageConfig;
    private ShipsConfig config;
    private DebugFile debugFile;

    public ShipsPlugin() {
        plugin = this;
    }

    public static ShipsPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void registerPlugin() {

    }

    @Override
    public void registerReady() {
        init();
        LegacyShipsConfig legacyShipsConfig = new LegacyShipsConfig();
        this.config = legacyShipsConfig.isLegacy() ? legacyShipsConfig.convertToNew() : new ShipsConfig();
        this.messageConfig = new MessageConfig();
        this.aMessageConfig = new AdventureMessageConfig();
        this.blockList = new DefaultBlockList();
        this.debugFile = new DebugFile();
        CorePlugin.getEventManager().register(this, new CoreEventListener());
        if (this.config.isFallingEnabled()) {
            Scheduler fallScheduler = FallExecutor.createScheduler();
            fallScheduler.run();
        }
        CorePlugin.createSchedulerBuilder().setDisplayName("Ships no gravity fix").setIteration(1).setIterationUnit(TimeUnit.SECONDS).setExecutor(AutoRunPatches.NO_GRAVITY_FIX).build(this);
        init2();
    }

    @Override
    public void registerCommands(CommandRegister register) {
        register.register(new ShipsArgumentCommand());
    }

    public abstract File getShipsConigFolder();

    @Deprecated
    public MessageConfig getMessageConfig() {
        return this.messageConfig;
    }

    public AdventureMessageConfig getAdventureMessageConfig() {
        return this.aMessageConfig;
    }

    public void loadCustomShipType() {
        File folder = new File(getShipsConigFolder(), "Configuration/ShipType/Custom");
        for (CloneableShipType<?> type : getAll(CloneableShipType.class)) {
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
        this.vessels.addAll(ShipsFileLoader.loadAll(Throwable::printStackTrace));
    }

    public void loadConverts() {
        getAll(VesselConverter.class).forEach(c -> {
            File folder = c.getFolder();
            File[] files = folder.listFiles();
            if (files == null) {
                return;
            }
            Stream.of(files).filter(f -> !f.isDirectory()).forEach(f -> {
                try {
                    registerVessel(c.convert(f));
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
        ConsoleSource source = CorePlugin.getConsole();
        source.sendMessage(AText.ofPlain("------[Ships Loaded Information][Start]------").withColour(NamedTextColours.RED));

        displayMessage(BasicBlockFinder.class, "BlockFinders", bf -> "");
        displayMessage(BasicMovement.class, "MovementMethods", bm -> "");
        displayMessage(BlockPriority.class, "BlockPriorities", bp -> bp.getPriorityNumber() + "");
        displayMessage(ShipsSign.class, "Signs", sn -> "");
        displayMessage(ShipType.class, "ShipTypes", st -> st.getDisplayName() + (st.getDisplayName().length() > 7 ? "\t" : "\t\t") + st.getFile().getFile().getPath());
        source.sendMessage(AText.ofPlain("Vessels: ").withColour(NamedTextColours.AQUA).append(AText.ofPlain("" + this.vessels.size()).withColour(NamedTextColours.YELLOW)));
        source.sendMessage(AText.ofPlain("------[Ships Loaded Information][End]------").withColour(NamedTextColours.RED));
    }

    private <I extends Identifiable> void displayMessage(Class<I> class1, String name, Function<I, String> function) {
        Set<I> values = getAll(class1);
        ConsoleSource source = CorePlugin.getConsole();
        source.sendMessage(AText.ofPlain("Found " + name + ": " + values.size()).withColour(NamedTextColours.AQUA));
        values.forEach(v -> {
            String id = v.getId();
            String text = function.apply(v);
            AText ret = AText.ofPlain("\t- " + id + (id.length() > 13 ? "\t" : "\t\t") + text).withColour(NamedTextColours.YELLOW);
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
        return identifiables.stream().filter(class1::isInstance).map(t -> (T) t).collect(Collectors.toSet());
    }

    public <T extends Identifiable> Optional<T> get(Class<T> class1) {
        return identifiables.stream().filter(class1::isInstance).map(t -> (T) t).findAny();
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
    public String getPluginName() {
        return "Ships";
    }

    @Override
    public String getPluginId() {
        return "ships";
    }

    @Override
    public String getPluginVersion() {
        return "6.0.0.0";
    }
}
