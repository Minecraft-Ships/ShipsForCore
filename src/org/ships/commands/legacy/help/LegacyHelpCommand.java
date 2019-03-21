package org.ships.commands.legacy.help;

import org.core.CorePlugin;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.commands.legacy.LegacyShipsCommand;

import java.util.ArrayList;
import java.util.List;

public class LegacyHelpCommand implements LegacyArgumentCommand {
    @Override
    public String getName() {
        return "Help";
    }

    @Override
    public boolean run(CommandSource source, String... args) {
        if(!(source instanceof CommandViewer)){
            return false;
        }
        CommandViewer commandViewer = (CommandViewer) source;
        LegacyShipsCommand lsc = (LegacyShipsCommand) CorePlugin.getServer().getCommands().stream().filter(c -> c.getName().equalsIgnoreCase("ships")).findAny().get();
        lsc.getArguments().forEach(c -> commandViewer.sendMessagePlain(c.getName()));
        return true;
    }

    @Override
    public List<String> tab(CommandSource source, String... args) {
        return new ArrayList<>();
    }
}
