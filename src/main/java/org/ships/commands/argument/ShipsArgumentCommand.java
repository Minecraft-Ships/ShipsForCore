package org.ships.commands.argument;

import org.core.command.ArgumentLauncher;
import org.core.command.CommandLauncher;
import org.core.command.argument.ArgumentCommand;
import org.core.entity.living.human.player.LivePlayer;
import org.core.platform.Plugin;
import org.core.source.command.CommandSource;
import org.ships.commands.argument.blockinfo.ShipsBlockInfoArgumentCommand;
import org.ships.commands.argument.fix.NoGravityArgumentCommand;
import org.ships.commands.argument.help.ShipsHelpArgumentCommand;
import org.ships.commands.argument.info.ShipsInfoArgumentCommand;
import org.ships.commands.argument.ship.blocklist.ShipsBlockListViewArgumentCommand;
import org.ships.commands.argument.ship.blocklist.ShipsBlockListViewBlockArgumentCommand;
import org.ships.commands.argument.ship.blocklist.set.ShipsBlockListSetBlockLimitArgumentCommand;
import org.ships.commands.argument.ship.blocklist.set.ShipsBlockListSetCollideTypeArgumentCommand;
import org.ships.commands.argument.ship.eot.ShipsShipEOTEnableArgumentCommand;
import org.ships.commands.argument.ship.info.ShipsShipInfoArgumentCommand;
import org.ships.commands.argument.ship.teleport.ShipsShipTeleportSetArgument;
import org.ships.commands.argument.ship.teleport.ShipsShipTeleportToArgument;
import org.ships.commands.argument.ship.track.ShipsShipTrackArgumentCommand;
import org.ships.commands.argument.ship.unlock.ShipsShipUnlockArgumentCommand;
import org.ships.plugin.ShipsPlugin;

import java.util.HashSet;
import java.util.Set;

public class ShipsArgumentCommand implements ArgumentLauncher, CommandLauncher {

    public static Set<ArgumentCommand> COMMANDS = new HashSet<>();

    static {
        COMMANDS.add(new ShipsInfoArgumentCommand());
        COMMANDS.add(new ShipsHelpArgumentCommand());
        COMMANDS.add(new ShipsBlockInfoArgumentCommand());

        COMMANDS.add(new ShipsBlockListViewBlockArgumentCommand());
        COMMANDS.add(new ShipsBlockListViewArgumentCommand());
        COMMANDS.add(new ShipsBlockListSetCollideTypeArgumentCommand());
        COMMANDS.add(new ShipsBlockListSetBlockLimitArgumentCommand());

        COMMANDS.add(new NoGravityArgumentCommand());

        COMMANDS.add(new ShipsShipInfoArgumentCommand());
        COMMANDS.add(new ShipsShipTrackArgumentCommand());
        COMMANDS.add(new ShipsShipTeleportToArgument());
        COMMANDS.add(new ShipsShipTeleportSetArgument());
        COMMANDS.add(new ShipsShipUnlockArgumentCommand());
        COMMANDS.add(new ShipsShipEOTEnableArgumentCommand());
    }

    @Override
    public Set<ArgumentCommand> getCommands() {
        return COMMANDS;
    }

    @Override
    public String getName() {
        return "Ships";
    }

    @Override
    public String getDescription() {
        return "Ships commands";
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        if(source instanceof LivePlayer){
            return ((LivePlayer) source).hasPermission("ships.cmd.ships");
        }
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return ShipsPlugin.getPlugin();
    }
}
