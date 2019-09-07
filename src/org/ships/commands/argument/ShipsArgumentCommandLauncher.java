package org.ships.commands.argument;

import org.core.command.CommandLauncher;
import org.core.command.argument.ArgumentCommandLauncher;
import org.core.platform.Plugin;
import org.core.source.command.CommandSource;
import org.ships.commands.argument.blockinfo.ShipsBlockInfoCommand;
import org.ships.commands.argument.info.ShipsInfoCommand;
import org.ships.commands.argument.shiptype.ShipsShipTypeCommand;
import org.ships.plugin.ShipsPlugin;

public class ShipsArgumentCommandLauncher extends ArgumentCommandLauncher implements CommandLauncher {

    public ShipsArgumentCommandLauncher(){
        super(new ShipsInfoCommand(), new ShipsBlockInfoCommand(), new ShipsShipTypeCommand());
    }

    @Override
    public String getName() {
        return "Ships";
    }

    @Override
    public String getDescription() {
        return "All Ships commands";
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        return this.commands.stream().anyMatch(c -> c.hasPermission(source));
    }

    @Override
    public Plugin getPlugin() {
        return ShipsPlugin.getPlugin();
    }
}
