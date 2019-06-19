package org.ships.plugin;

import org.core.CorePlugin;
import org.core.platform.Plugin;
import org.core.text.TextColours;
import org.core.utils.Identifable;
import org.ships.algorthum.blockfinder.BasicBlockFinder;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.commands.legacy.LegacyShipsCommand;
import org.ships.config.blocks.DefaultBlockList;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.debug.DebugFile;
import org.ships.listener.core.CoreEventListener;
import org.ships.movement.BlockPriority;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.sign.*;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ShipsPlugin implements Plugin {

    private static ShipsPlugin plugin;
    private Set<Identifable> identifable = new HashSet<>();
    private DefaultBlockList blockList;
    private ShipsConfig config;
    private DebugFile debugFile;

    public ShipsPlugin(){
        plugin = this;
        init();
        this.config = new ShipsConfig();
        this.blockList = new DefaultBlockList();
        this.debugFile = new DebugFile();
    }

    public abstract File getShipsConigFolder();

    private void init(){
        this.identifable.add(BasicMovement.SHIPS_FIVE);
        this.identifable.add(BasicMovement.SHIPS_SIX);
        this.identifable.add(BasicBlockFinder.SHIPS_FIVE);
        this.identifable.add(BasicBlockFinder.SHIPS_SIX);
        this.identifable.add(BlockPriority.AIR);
        this.identifable.add(BlockPriority.DIRECTIONAL);
        this.identifable.add(BlockPriority.ATTACHED);
        this.identifable.add(BlockPriority.NORMAL);
        this.identifable.add(new LicenceSign());
        this.identifable.add(new AltitudeSign());
        this.identifable.add(new WheelSign());
        this.identifable.add(new MoveSign());
        this.identifable.add(new EOTSign());
        this.identifable.add(ShipType.OVERPOWERED_SHIP);
        this.identifable.add(ShipType.AIRSHIP);
        this.identifable.add(ShipType.WATERSHIP);

        CorePlugin.getEventManager().register(this, new CoreEventListener());
        CorePlugin.getServer().registerCommands(new LegacyShipsCommand());
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
        displayMessage(ShipType.class, "ShipTypes", st -> st.getDisplayName() + "\t" + st.getFile().getFile().getPath());
        CorePlugin.getConsole().sendMessage(CorePlugin.buildText(TextColours.RED + "------[Ships Loaded Information][End]------"));
    }

    private <I extends Identifable> void displayMessage(Class<I> class1, String name, Function<I, String> function){
        Set<I> values = getAll(class1);
        CorePlugin.getConsole().sendMessage(CorePlugin.buildText(TextColours.AQUA + "Found " + name + ": " + values.size()));
        values.forEach(v -> CorePlugin.getConsole().sendMessage(CorePlugin.buildText(TextColours.YELLOW + "\t- " + v.getId() + "\t" + function.apply(v))));
    }

    public DebugFile getDebugFile(){
        return this.debugFile;
    }

    public DefaultBlockList getBlockList(){
        return this.blockList;
    }

    public <T extends Identifable> Set<T> getAll(Class<T> class1){
        return (Set<T>)identifable.stream().filter(class1::isInstance).collect(Collectors.toSet());
    }

    public <T extends Identifable> Optional<T> get(Class<T> class1){
        return (Optional<T>)identifable.stream().filter(class1::isInstance).findAny();
    }

    public void register(Identifable... identifables){
        identifable.addAll(Arrays.asList(identifables));
    }

    public static ShipsPlugin getPlugin(){
        return plugin;
    }

    public static <T> String toString(Collection<T> collection, String split, Function<T, String> asString){
        StringBuilder ret = null;
        for(T value : collection){
            if(ret == null){
                ret = new StringBuilder(asString.apply(value));
            }else{
                ret.append(split).append(asString.apply(value));
            }
        }
        return Objects.requireNonNull(ret).toString();
    }

    @Override
    public String getPluginName() {
        return "Ships";
    }

    @Override
    public String getPluginVersion(){
        return "6.0.0.0";
    }
}
