package org.ships.commands.legacy;

import org.core.command.BaseCommandLauncher;
import org.core.platform.Plugin;
import org.core.source.command.CommandSource;
import org.ships.commands.legacy.autopilot.LegacyAutoPilotCommand;
import org.ships.commands.legacy.blockinfo.LegacyBlockInfoCommand;
import org.ships.commands.legacy.blocklist.LegacyBlockListCommand;
import org.ships.commands.legacy.cleanup.LegacyCleanupCommand;
import org.ships.commands.legacy.help.LegacyHelpCommand;
import org.ships.commands.legacy.info.LegacyInfoCommand;
import org.ships.commands.legacy.ship.LegacyShipCommand;
import org.ships.plugin.ShipsPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LegacyShipsCommand implements BaseCommandLauncher {

    private final List<LegacyArgumentCommand> arguments = Arrays.asList(new LegacyShipCommand(), new LegacyCleanupCommand(), new LegacyInfoCommand(), new LegacyBlockListCommand(), new LegacyBlockInfoCommand(), new LegacyAutoPilotCommand());

    public List<LegacyArgumentCommand> getArguments(){
        return this.arguments;
    }

    @Override
    public String getName() {
        return "ships";
    }

    @Override
    public String getDescription() {
        return "All ships commands";
    }

    @Override
    public String getPermission() {
        return "ships.cmd.ships";
    }

    @Override
    public String getUsage(CommandSource source) {
        return "Ships <command>";
    }

    @Override
    public Plugin getPlugin() {
        return ShipsPlugin.getPlugin();
    }

    @Override
    public boolean run(CommandSource source, String... args) {
        if(args.length == 0){
            new LegacyHelpCommand().run(source, args);
        }else{
            String argument = args[0];
            Optional<LegacyArgumentCommand> opCommand = this.arguments.stream().filter(a -> argument.equalsIgnoreCase(a.getName())).findAny();
            if(opCommand.isPresent()){
                opCommand.get().run(source, args);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> tab(CommandSource source, String... args) {
        List<String> list = new ArrayList<>();
        if(args.length == 0){
            this.arguments.forEach(c -> list.add(c.getName()));
        }else if(args.length == 1){
            this.arguments.stream().filter(c -> c.getName().toLowerCase().startsWith(args[0])).forEach(c -> list.add(c.getName()));
        }else{
            String argument = args[0];
            Optional<LegacyArgumentCommand> opCommand = this.arguments.stream().filter(a -> argument.equalsIgnoreCase(a.getName())).findAny();
            if(opCommand.isPresent()){
                return opCommand.get().tab(source, args);
            }
        }
        return list;
    }
}
