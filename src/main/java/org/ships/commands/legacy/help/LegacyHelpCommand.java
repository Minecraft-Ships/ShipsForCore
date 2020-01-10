package org.ships.commands.legacy.help;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.commands.legacy.LegacyShipsCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LegacyHelpCommand implements LegacyArgumentCommand {
    @Override
    public String getName() {
        return "Help";
    }

    @Override
    public Optional<String> getPermission() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandSource source, String... args) {
        if(!(source instanceof CommandViewer)){
            return false;
        }
        CommandViewer commandViewer = (CommandViewer) source;
        LegacyShipsCommand lsc = (LegacyShipsCommand) CorePlugin.getServer().getCommands().stream().filter(c -> c.getName().equalsIgnoreCase("ships")).findAny().get();
        lsc.getArguments().stream().filter(c -> {
            Optional<String> opPermission = c.getPermission();
            if(opPermission.isPresent()) {
                if (source instanceof LivePlayer) {
                    return ((LivePlayer) source).hasPermission(opPermission.get());
                }
            }
            return true;
        }).forEach(c -> commandViewer.sendMessagePlain(c.getName()));
        return true;
    }

    @Override
    public List<String> tab(CommandSource source, String... args) {
        return new ArrayList<>();
    }
}
