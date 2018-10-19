package org.bships.plugin;

import org.ships.plugin.ShipsPlugin;

import java.io.File;

public class ShipsBPlugin extends ShipsPlugin {
    @Override
    public Object getBukkitLauncher() {
        return ShipsMain.getPlugin();
    }

    @Override
    public Object getSpongeLauncher() {
        return null;
    }

    @Override
    public File getShipsConigFolder() {
        return new File("plugins/Ships");
    }
}
