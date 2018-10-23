package org.bships.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.ships.implementation.bukkit.CoreToBukkit;

public class ShipsMain extends JavaPlugin {

    private static ShipsMain plugin;

    public void onEnable(){
        plugin = this;
        new CoreToBukkit(this);
        ShipsBPlugin plugin = new ShipsBPlugin();
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> plugin.getLoadedMessages());
    }

    public static ShipsMain getPlugin(){
        return plugin;
    }
}
