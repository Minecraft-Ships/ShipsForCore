package org.ships.commands.arg;

import org.core.command.CommandLauncher;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentLauncher;
import org.core.command.argument.CommandContext;
import org.core.command.argument.arg.child.ChildArgument;
import org.core.command.argument.arg.child.MatchArgument;
import org.core.platform.Plugin;
import org.core.source.command.CommandSource;
import org.ships.commands.arg.blockinfo.ArgumentBlockInfoCommand;
import org.ships.commands.arg.ship.ShipArgumentCommand;
import org.ships.plugin.ShipsPlugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ShipsArgumentCommand extends CommandArgumentLauncher.MatchLauncher implements CommandLauncher {

    private static Set<ChildArgument> DEFAULT_ARGUMENTS = new HashSet<>(Arrays.asList(new ChildArgument.Arg("Ship", new ShipArgumentCommand()), new ChildArgument.Arg("BlockInfo", new ArgumentBlockInfoCommand())));

    public ShipsArgumentCommand() {
        super("Ships", "All Ships Commands", new CommandContext(new MatchArgument("Match", DEFAULT_ARGUMENTS)));
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return ShipsPlugin.getPlugin();
    }

    public static void register(ChildArgument arg){
        DEFAULT_ARGUMENTS.add(arg);
    }

}
