package org.ships.commands.legacy;

import org.core.CorePlugin;
import org.core.command.CommandLauncher;
import org.core.entity.living.human.player.LivePlayer;
import org.core.platform.Plugin;
import org.core.source.command.CommandSource;
import org.core.text.TextColours;
import org.ships.commands.legacy.blockinfo.LegacyBlockInfoCommand;
import org.ships.commands.legacy.blocklist.LegacyBlockListCommand;
import org.ships.commands.legacy.config.LegacyConfigCommand;
import org.ships.commands.legacy.fix.FixLegacyCommand;
import org.ships.commands.legacy.help.LegacyHelpCommand;
import org.ships.commands.legacy.info.LegacyInfoCommand;
import org.ships.commands.legacy.ship.LegacyShipCommand;
import org.ships.commands.legacy.shiptype.LegacyShipTypeCommand;
import org.ships.plugin.ShipsPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Deprecated
public class LegacyShipsCommand implements CommandLauncher {

    public static final List<LegacyArgumentCommand> ARGUMENTS = Arrays.asList(new FixLegacyCommand(), new LegacyConfigCommand(), new LegacyShipCommand(), new LegacyShipTypeCommand(), new LegacyInfoCommand(), new LegacyBlockListCommand(), new LegacyBlockInfoCommand());

    public List<LegacyArgumentCommand> getArguments(){
        return ARGUMENTS;
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
    public boolean hasPermission(CommandSource source) {
        if(source instanceof LivePlayer){
            return ((LivePlayer) source).hasPermission("ships.cmd.ships");
        }
        return true;
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
            Optional<LegacyArgumentCommand> opCommand = this.getArguments().stream().filter(a -> argument.equalsIgnoreCase(a.getName())).findAny();
            if(opCommand.isPresent()){
                LegacyArgumentCommand command = opCommand.get();
                if(command.getPermission().isPresent() && source instanceof LivePlayer && !((LivePlayer)source).hasPermission(command.getPermission().get())){
                    ((LivePlayer)source).sendMessage(CorePlugin.buildText(TextColours.RED + "You do not have permission for that command"));
                    return false;
                }
                command.run(source, args);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> tab(CommandSource source, String... args) {
        List<String> list = new ArrayList<>();
        if(args.length == 0){
            this.getArguments().forEach(c -> list.add(c.getName()));
        }else if(args.length == 1){
            this.getArguments().stream().filter(c -> c.getName().toLowerCase().startsWith(args[0])).forEach(c -> list.add(c.getName()));
        }else{
            String argument = args[0];
            Optional<LegacyArgumentCommand> opCommand = this.getArguments().stream().filter(a -> argument.equalsIgnoreCase(a.getName())).findAny();
            if(opCommand.isPresent()){
                LegacyArgumentCommand command = opCommand.get();
                if(command.getPermission().isPresent() && source instanceof LivePlayer && !((LivePlayer)source).hasPermission(command.getPermission().get())){
                    ((LivePlayer)source).sendMessage(CorePlugin.buildText(TextColours.RED + "You do not have permission for that command"));
                    return new ArrayList<>();
                }
                return command.tab(source, args);
            }
        }
        return list;
    }
}
