package org.ships.commands.argument;

import org.core.command.CommandLauncher;
import org.core.command.argument.ArgumentCommandLauncher;
import org.core.platform.Plugin;
import org.ships.commands.argument.blockinfo.ShipsBlockInfoCommand;
import org.ships.commands.argument.info.ShipsInfoCommand;
import org.ships.commands.argument.shiptype.ShipsShipTypeCommand;
import org.ships.plugin.ShipsPlugin;

import java.util.Optional;

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
    public Optional<String> getPermission() {
        return Optional.empty();
    }

    @Override
    public Plugin getPlugin() {
        return ShipsPlugin.getPlugin();
    }
}
