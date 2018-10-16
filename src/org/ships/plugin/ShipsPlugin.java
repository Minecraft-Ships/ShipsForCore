package org.ships.plugin;

import org.core.platform.Plugin;
import org.core.utils.Identifable;
import org.ships.algorthum.blockfinder.Ships5BlockFinder;
import org.ships.algorthum.movement.Ships5Movement;
import org.ships.algorthum.movement.Ships6Movement;
import org.ships.config.blocks.BlockList;
import org.ships.config.blocks.DefaultBlockList;
import org.ships.movement.BlockPriority;
import org.ships.vessel.sign.LicenceSign;

import java.util.*;
import java.util.function.Function;

public class ShipsPlugin implements Plugin {

    private static ShipsPlugin plugin;
    private Set<Identifable> identifable = new HashSet<>();
    private DefaultBlockList blockList;

    public ShipsPlugin(){
        plugin = this;
    }

    public DefaultBlockList getBlockList(){
        return this.blockList;
    }

    private void init(){
        this.identifable.add(new Ships5Movement());
        this.identifable.add(new Ships6Movement());
        this.identifable.add(new Ships5BlockFinder());
        this.identifable.add(BlockPriority.AIR);
        this.identifable.add(BlockPriority.ATTACHED);
        this.identifable.add(BlockPriority.NORMAL);
        this.identifable.add(new LicenceSign());
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

    @Override
    public Object getBukkitLauncher() {
        return null;
    }

    @Override
    public Object getSpongeLauncher() {
        return null;
    }
}
