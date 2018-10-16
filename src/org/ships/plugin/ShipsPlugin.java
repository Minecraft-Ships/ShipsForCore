package org.ships.plugin;

import org.core.platform.Plugin;
import org.core.utils.Identifable;

import java.util.*;
import java.util.function.Function;

public class ShipsPlugin implements Plugin {

    private static ShipsPlugin plugin;
    private Set<Identifable> identifable = new HashSet<>();

    public ShipsPlugin(){
        plugin = this;
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
        return null;
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
