package org.ships.commands.arg.ship;

import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentLauncher;
import org.core.command.argument.CommandContext;
import org.core.command.argument.arg.child.ChildArgument;
import org.core.command.argument.arg.child.MatchArgument;
import org.core.source.command.CommandSource;
import org.ships.commands.arg.arguments.ShipArgument;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ShipArgumentCommand extends CommandArgumentLauncher.MatchLauncher {

    public static final Set<ChildArgument> SHIPS_ARGUMENTS = new HashSet<>(Arrays.asList(new ChildArgument.Arg("Track", new TrackShipArgumentCommand())));

    public ShipArgumentCommand() {
        super("shipid", "Ship specific", new CommandContext(new ShipArgument("vessel", ShipArgument.CAPTAIN_OF), new MatchArgument("arg", SHIPS_ARGUMENTS)));
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        return true;
    }
}
