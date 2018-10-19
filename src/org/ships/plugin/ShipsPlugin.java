package org.ships.plugin;

import org.core.CorePlugin;
import org.core.platform.Plugin;
import org.core.utils.Identifable;
import org.ships.algorthum.blockfinder.Ships5BlockFinder;
import org.ships.algorthum.movement.Ships5Movement;
import org.ships.algorthum.movement.Ships6Movement;
import org.ships.config.blocks.DefaultBlockList;
import org.ships.listener.core.CoreEventListener;
import org.ships.movement.BlockPriority;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.sign.LicenceSign;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ShipsPlugin implements Plugin {

    private static ShipsPlugin plugin;
    private Set<Identifable> identifable = new HashSet<>();
    private DefaultBlockList blockList = new DefaultBlockList();

    public ShipsPlugin(){
        plugin = this;
        init();
    }

    public abstract File getShipsConigFolder();

    private void init(){
        this.identifable.add(new Ships5Movement());
        this.identifable.add(new Ships6Movement());
        this.identifable.add(new Ships5BlockFinder());
        this.identifable.add(BlockPriority.AIR);
        this.identifable.add(BlockPriority.ATTACHED);
        this.identifable.add(BlockPriority.NORMAL);
        this.identifable.add(new LicenceSign());
        this.identifable.add(ShipType.OVERPOWERED_SHIP);

        CorePlugin.getEventManager().register(this, new CoreEventListener());
    }

    public DefaultBlockList getBlockList(){
        return this.blockList;
    }

    public <T extends Identifable> Set<T> getAll(Class<T> class1){
        return (Set<T>)identifable.stream().filter(i -> class1.isInstance(i)).collect(Collectors.toSet());
    }

    public <T extends Identifable> Optional<T> get(Class<T> class1){
        return (Optional<T>)identifable.stream().filter(i -> class1.isInstance(i)).findAny();
    }

    public void register(Identifable... identifables){
        identifable.addAll(Arrays.asList(identifables));
    }

    public static ShipsPlugin getPlugin(){
        return plugin;
    }

    public static <T extends Object> String toString(Collection<T> collection, String split, Function<T, String> asString){
        String ret = null;
        for(T value : collection){
            if(ret == null){
                ret = asString.apply(value);
            }else{
                ret = ret + split + asString.apply(value);
            }
        }
        return ret;
    }

    @Override
    public String getPluginName() {
        return "Ships";
    }
}
