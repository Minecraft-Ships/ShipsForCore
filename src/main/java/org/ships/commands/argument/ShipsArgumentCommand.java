package org.ships.commands.argument;

import org.core.command.ArgumentLauncher;
import org.core.command.CommandLauncher;
import org.core.command.argument.ArgumentCommand;
import org.core.entity.living.human.player.LivePlayer;
import org.core.platform.Plugin;
import org.core.source.command.CommandSource;
import org.core.utils.Singleton;
import org.ships.commands.argument.blockinfo.ShipsBlockInfoArgumentCommand;
import org.ships.commands.argument.blocklist.ShipsBlockListViewArgumentCommand;
import org.ships.commands.argument.blocklist.ShipsBlockListViewBlockArgumentCommand;
import org.ships.commands.argument.blocklist.set.ShipsBlockListSetBlockLimitArgumentCommand;
import org.ships.commands.argument.blocklist.set.ShipsBlockListSetCollideTypeArgumentCommand;
import org.ships.commands.argument.config.AbstractShipsConfigSetArgument;
import org.ships.commands.argument.config.AbstractShipsConfigViewArgument;
import org.ships.commands.argument.config.shiptype.ShipTypeSetSingleConfigArgument;
import org.ships.commands.argument.config.shiptype.ShipTypeViewSingleConfigArgument;
import org.ships.commands.argument.fix.NoGravityArgumentCommand;
import org.ships.commands.argument.help.ShipsHelpArgumentCommand;
import org.ships.commands.argument.info.ShipsInfoArgumentCommand;
import org.ships.commands.argument.ship.autopilot.CancelAutopilotCommand;
import org.ships.commands.argument.ship.autopilot.DeployAutopilotCommand;
import org.ships.commands.argument.ship.crew.ShipAddCrewArgumentCommand;
import org.ships.commands.argument.ship.crew.ShipViewCrewArgumentCommand;
import org.ships.commands.argument.ship.eot.ShipsShipEOTEnableArgumentCommand;
import org.ships.commands.argument.ship.info.ShipsShipInfoArgumentCommand;
import org.ships.commands.argument.ship.moveto.ShipsMoveToAdditionArgument;
import org.ships.commands.argument.ship.moveto.ShipsMoveToExactArgument;
import org.ships.commands.argument.ship.moveto.ShipsMoveToRotateArgument;
import org.ships.commands.argument.ship.teleport.ShipsShipTeleportSetArgument;
import org.ships.commands.argument.ship.teleport.ShipsShipTeleportToArgument;
import org.ships.commands.argument.ship.track.ShipsShipTrackArgumentCommand;
import org.ships.commands.argument.ship.unlock.ShipsShipUnlockArgumentCommand;
import org.ships.commands.argument.type.ShipsCreateShipTypeArgument;
import org.ships.commands.argument.type.ShipsDeleteShipTypeArgument;
import org.ships.commands.argument.type.ShipsViewShipTypeArgument;
import org.ships.commands.argument.type.flag.ModifyShipTypeFlagArgument;
import org.ships.commands.argument.type.flag.ViewShipTypeFlagArgument;
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

        COMMANDS.add(new AbstractShipsConfigViewArgument(new Singleton<>(() -> ShipsPlugin.getPlugin().getConfig()), "config", "configuration"));
        COMMANDS.add(new AbstractShipsConfigViewArgument(new Singleton<>(() -> ShipsPlugin.getPlugin().getMessageConfig()), "messages"));
        COMMANDS.add(new AbstractShipsConfigSetArgument(new Singleton<>(() -> ShipsPlugin.getPlugin().getConfig()), "config", "configuration"));
        COMMANDS.add(new AbstractShipsConfigSetArgument(new Singleton<>(() -> ShipsPlugin.getPlugin().getMessageConfig()), "messages"));
        COMMANDS.add(new ShipTypeViewSingleConfigArgument());
        COMMANDS.add(new ShipTypeSetSingleConfigArgument());
        COMMANDS.add(new ViewShipTypeFlagArgument());
        COMMANDS.add(new ModifyShipTypeFlagArgument());

        COMMANDS.add(new ShipsShipInfoArgumentCommand());
        COMMANDS.add(new ShipsShipTrackArgumentCommand());
        COMMANDS.add(new ShipsShipTeleportToArgument());
        COMMANDS.add(new ShipsShipTeleportSetArgument());
        COMMANDS.add(new ShipsShipUnlockArgumentCommand());
        COMMANDS.add(new ShipsShipEOTEnableArgumentCommand());
        COMMANDS.add(new ShipViewCrewArgumentCommand());
        COMMANDS.add(new ShipAddCrewArgumentCommand());
        COMMANDS.add(new CancelAutopilotCommand());
        COMMANDS.add(new DeployAutopilotCommand());

        COMMANDS.add(new ShipsCreateShipTypeArgument());
        COMMANDS.add(new ShipsViewShipTypeArgument());
        COMMANDS.add(new ShipsDeleteShipTypeArgument());

        COMMANDS.add(new ShipsMoveToExactArgument());
        COMMANDS.add(new ShipsMoveToAdditionArgument());
        COMMANDS.add(new ShipsMoveToRotateArgument());
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
        if (source instanceof LivePlayer) {
            return ((LivePlayer) source).hasPermission("ships.cmd.ships");
        }
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return ShipsPlugin.getPlugin();
    }
}
