package org.ships.commands.legacy.cleanup;

import org.core.CorePlugin;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.vessel.common.loader.ShipsFileLoader;
import org.ships.vessel.common.types.AbstractShipsVessel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LegacyCleanupCommand implements LegacyArgumentCommand {
    @Override
    public String getName() {
        return "cleanup";
    }

    @Override
    public boolean run(CommandSource source, String... args) {
        if(!(source instanceof CommandViewer)){
            return false;
        }
        CommandViewer viewer = (CommandViewer)source;
        if(args.length == 1){
            return false;
        }
        if(args[1].equalsIgnoreCase("check")){
            viewer.sendMessage(CorePlugin.buildText(TextColours.AQUA + "--[Failed loading]--"));
            Set<AbstractShipsVessel> ships = ShipsFileLoader.loadAll(e -> viewer.sendMessagePlain("  - " + (e.getFile().isPresent() ? e.getFile().get().getName() : e.getReason())));
            viewer.sendMessage(CorePlugin.buildText(TextColours.AQUA + "--[Duplicated position]--"));
            ships.stream()
                    .forEach(s -> ships.stream()
                            .filter(s1 -> !s1.equals(s))
                            .filter(s1 -> s.getStructure().getPositions().stream()
                                    .anyMatch(p -> s1.getStructure().getPositions().stream()
                                            .anyMatch(p1 -> p1.equals(p))))
                            .forEach(e -> viewer.sendMessagePlain("  - " + (e.getId()))));

            return true;
        }
        return false;
    }

    @Override
    public List<String> tab(CommandSource source, String... args) {
        List<String> list = new ArrayList<>();
        if(args.length == 2 && args[1].equalsIgnoreCase("")){
            list.add("check");
            return list;
        }else if(args.length == 2){
            if("check".startsWith(args[1])){
                list.add("check");
            }
            return list;
        }
        return list;
    }
}
