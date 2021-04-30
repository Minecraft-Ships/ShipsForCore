package org.ships.plugin;

import org.core.CorePlugin;
import org.core.command.CommandRegister;
import org.core.platform.Plugin;
import org.core.schedule.Scheduler;
import org.core.schedule.unit.TimeUnit;
import org.core.text.Text;
import org.core.text.TextColours;
import org.core.utils.Identifable;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.blockfinder.BasicBlockFinder;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.commands.argument.ShipsArgumentCommand;
import org.ships.commands.legacy.LegacyShipsCommand;
import org.ships.config.blocks.DefaultBlockList;
import org.ships.config.configuration.LegacyShipsConfig;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.debug.DebugFile;
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
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.converts.ShipsConverter;
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

    private static ShipsPlugin plugin;
    private final Map<String, VesselFlag.Builder<?, ?>> vesselFlags = new HashMap<>();
    private final Set<Identifable> identifable = new HashSet<>();
    private final Set<CrewPermission> defaultPermissions = new HashSet<>(Arrays.asList(CrewPermission.CAPTAIN, CrewPermission.CREW_MEMBER));
    private final DefaultBlockList blockList;
    private final MessageConfig messageConfig;
    private final ShipsConfig config;
    private final DebugFile debugFile;
    private final Set<Vessel> vessels = new HashSet<>();

    public static final double PRERELEASE_VERSION = 11;
    public static final String PRERELEASE_TAG = "Beta";

    public ShipsPlugin(){
        plugin = this;
        init();
        LegacyShipsConfig legacyShipsConfig = new LegacyShipsConfig();
        this.config = legacyShipsConfig.isLegacy() ? legacyShipsConfig.convertToNew() : new ShipsConfig();
        this.messageConfig = new MessageConfig();
        this.blockList = new DefaultBlockList();
        this.debugFile = new DebugFile();
        CorePlugin.getEventManager().register(this, new CoreEventListener());
        if(this.config.isFallingEnabled()) {
            Scheduler fallScheduler = FallExecutor.createScheduler();
            fallScheduler.run();
        }
        CorePlugin.createSchedulerBuilder().setDisplayName("Ships no gravity fix").setIteration(1).setIterationUnit(TimeUnit.SECONDS).setExecutor(AutoRunPatches.NO_GRAVITY_FIX).build(this);
        init2();
    }

    @Override
    public void registerCommands(CommandRegister register) {
        if(this.config.getFile().getBoolean(this.config.ALPHA_COMMAND_USE_LEGACY.getNode()).orElse(true)) {
            register.register(new LegacyShipsCommand());
        }else{
            register.register(new ShipsArgumentCommand());
        }
    }

    public abstract File getShipsConigFolder();

    public MessageConfig getMessageConfig(){
        return this.messageConfig;
    }

    public void loadCustomShipType(){
        File folder = new File(getShipsConigFolder(), "Configuration/ShipType/Custom");
        for(CloneableShipType<?> type : getAll(CloneableShipType.class)){
            File folderType = new File(folder, type.getId().replace(":", ".") + "/");
            File[] files = folderType.listFiles();
            if(files == null){
                folderType.mkdirs();
                continue;
            }
            for(File file : files){
                this.identifable.add(type.cloneWithName(file));
            }
        }
    }

    public void loadVessels(){
        this.vessels.addAll(ShipsFileLoader.loadAll(Throwable::printStackTrace));
    }

    public void loadConverts(){
        getAll(ShipsConverter.class).forEach(c -> {
            File folder = c.getFolder();
            File[] files = folder.listFiles();
            if(files == null){
                return;
            }
            Stream.of(files).filter(f -> !f.isDirectory()).forEach(f -> {
                if(c instanceof VesselConverter){
                    try {
                        registerVessel((Vessel)c.convert(f));
                    } catch (IOException e) {
                        System.err.println("Error converting vessel with " + c.getId() + " at: " + f.getPath());
                        e.printStackTrace();
                    }
                }else{
                    try {
                        System.err.println("Error converting file with " + c.getId() + " at: " + f.getPath());
                        register(c.convert(f));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private void init(){
        this.identifable.add(BasicMovement.SHIPS_FIVE);
        this.identifable.add(BasicMovement.SHIPS_SIX);
        this.identifable.add(BasicBlockFinder.SHIPS_FIVE);
        this.identifable.add(BasicBlockFinder.SHIPS_FIVE_ASYNC);
        this.identifable.add(BasicBlockFinder.SHIPS_SIX);
        this.identifable.add(BasicBlockFinder.SHIPS_SIX_RELEASE_ONE_ASYNC);
        this.identifable.add(BlockPriority.AIR);
        this.identifable.add(BlockPriority.DIRECTIONAL);
        this.identifable.add(BlockPriority.ATTACHED);
        this.identifable.add(BlockPriority.NORMAL);
        this.identifable.add(new Ships5VesselConverter());
        this.identifable.add(new LicenceSign());
        this.identifable.add(new AltitudeSign());
        this.identifable.add(new WheelSign());
        this.identifable.add(new MoveSign());
        this.identifable.add(new EOTSign());
        this.vesselFlags.put("ships:player_states", new PlayerStatesFlag.Builder());
    }

    private void init2(){
        this.identifable.add(ShipType.OVERPOWERED_SHIP);
        this.identifable.add(ShipType.AIRSHIP);
        this.identifable.add(ShipType.WATERSHIP);
        this.identifable.add(ShipType.SUBMARINE);
        this.identifable.add(ShipType.MARSSHIP);
        this.identifable.add(ShipType.PLANE);
    }

    public ShipsConfig getConfig(){
        return this.config;
    }

    public void getLoadedMessages(){
        CorePlugin.getConsole().sendMessage(CorePlugin.buildText(TextColours.RED + "------[Ships Loaded Information][Start]------"));
        displayMessage(BasicBlockFinder.class, "BlockFinders", bf -> "");
        displayMessage(BasicMovement.class, "MovementMethods", bm -> "");
        displayMessage(BlockPriority.class, "BlockPriorities", bp -> bp.getPriorityNumber() + "");
        displayMessage(ShipsSign.class, "Signs", sn -> "");
        displayMessage(ShipType.class, "ShipTypes", st -> st.getDisplayName() + (st.getDisplayName().length() > 7 ? "\t" : "\t\t") + st.getFile().getFile().getPath());
        CorePlugin.getConsole().sendMessage(CorePlugin.buildText(TextColours.AQUA + "Vessels: " + TextColours.YELLOW + this.vessels.size()));
        CorePlugin.getConsole().sendMessage(CorePlugin.buildText(TextColours.RED + "------[Ships Loaded Information][End]------"));
    }

    private <I extends Identifable> void displayMessage(Class<I> class1, String name, Function<I, String> function){
        Set<I> values = getAll(class1);
        CorePlugin.getConsole().sendMessage(CorePlugin.buildText(TextColours.AQUA + "Found " + name + ": " + values.size()));
        values.forEach(v -> {
            String id = v.getId();
            String text = function.apply(v);
            Text ret = CorePlugin.buildText(TextColours.YELLOW + "\t- " + id + (id.length() > 13 ? "\t" : "\t\t") + text);
            CorePlugin.getConsole().sendMessage(ret);
        });
    }

    public DebugFile getDebugFile(){
        return this.debugFile;
    }

    public DefaultBlockList getBlockList(){
        return this.blockList;
    }

    public Set<CrewPermission> getDefaultPermissions(){
        return this.defaultPermissions;
    }

    public Set<Vessel> getVessels(){
        return this.vessels;
    }

    public <T extends Identifable> Set<T> getAll(Class<T> class1){
        if(ShipsVessel.class.isAssignableFrom(class1)){
            return (Set<T>)(Set<? extends Vessel>)this.vessels.stream().filter(v -> v instanceof ShipsVessel).collect(Collectors.toSet());
        }
        return (Set<T>)identifable.stream().filter(class1::isInstance).collect(Collectors.toSet());
    }

    public <T extends Identifable> Optional<T> get(Class<T> class1){
        return (Optional<T>)identifable.stream().filter(class1::isInstance).findAny();
    }

    public Map<String, VesselFlag.Builder<?, ?>> getVesselFlags(){
        return this.vesselFlags;
    }

    public void registerVessel(@NotNull Vessel vessel){
        this.vessels.add(vessel);
    }

    public void unregisterVessel(Vessel... vessels){
        this.vessels.removeAll(Arrays.asList(vessels));
    }

    public void register(Identifable... identifables){
        this.identifable.addAll(Arrays.asList(identifables));
    }

    public void unregister(Identifable... identifables){
        this.identifable.removeAll(Arrays.asList(identifables));
    }

    public void register(CrewPermission... permissions){
        this.defaultPermissions.addAll(Arrays.asList(permissions));
    }

    public void register(String id, VesselFlag.Builder<?, ?> flag){
        this.vesselFlags.put(id, flag);
    }

    public static ShipsPlugin getPlugin(){
        return plugin;
    }

    @Override
    public String getPluginName() {
        return "Ships";
    }

    @Override
    public String getPluginId(){
        return "ships";
    }

    @Override
    public String getPluginVersion(){
        return "6.0.0.0";
    }
}
