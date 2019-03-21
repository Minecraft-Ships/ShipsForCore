package org.ships.commands.legacy.data;

import org.core.CorePlugin;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsFileLoader;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LegacyDataCommand implements LegacyArgumentCommand {
    @Override
    public String getName() {
        return "Data";
    }

    @Override
    public boolean run(CommandSource source, String... args) {
        if(!(source instanceof CommandViewer)){
            return false;
        }
        List<Vessel> vessels = new ArrayList<>();
        if(args.length == 0){
            vessels.addAll(ShipsFileLoader.loadAll());
        }else{
            for(int A = 0; A < args.length; A++) {
                int B = A;
                vessels.addAll(ShipsFileLoader.loadAll().stream().filter(t -> t.getName().equalsIgnoreCase(args[B])).collect(Collectors.toList()));
            }
        }
        CommandViewer viewer = (CommandViewer)source;
        if(vessels.isEmpty()){
            if(args.length != 1) {
                String shipName = args[1];
                Set<File> files = ShipsFileLoader.getFilesFromName(shipName);
                if(!files.isEmpty()){
                    File shipFile = files.iterator().next();
                    ShipsFileLoader sfl = new ShipsFileLoader(shipFile);
                    try{
                        Vessel vessel = sfl.load();
                        viewer.sendMessage(CorePlugin.buildText(TextColours.RED + " Could not get info of " + vessel.getName() + ". It is loading correctly"));
                    }catch (IOException e){
                        viewer.sendMessage(CorePlugin.buildText(TextColours.RED + " Error loading ship. " + e.getMessage()));
                    }
                    return true;
                }
            }
            viewer.sendMessagePlain("No Ships found");
            return true;
        }
        vessels.forEach(v -> {
            viewer.sendMessage(CorePlugin.buildText(TextColours.RED + "----[" + v.getName() + "]----"));
            viewer.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Type: " + TextColours.AQUA + v.getType().getId()));
            viewer.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Size: " + TextColours.AQUA + (v.getStructure().getPositions().size())));

        });
        return false;
    }

    @Override
    public List<String> tab(CommandSource source, String... args) {
        List<String> list = new ArrayList<>();
        if(args.length == 1){
            list.addAll(getTabbed(null));
        }else if(args.length == 2){
            list.addAll(getTabbed(args[1]));
        }
        return list;
    }

    private List<String> getTabbed(String filter){
        List<String> list = new ArrayList<>();
        ShipsPlugin.getPlugin().getAll(ShipType.class).stream().forEach(t -> {
            File folder = new File(ShipsFileLoader.getVesselDataFolder(), t.getId().replace(":", "."));
            File[] files = folder.listFiles();
            if(files == null){
                return;
            }
            for(File file : files){
                String name = file.getName();
                int length = name.length();
                for(int A = length; A < 0; A--){
                    if(name.charAt(A - 1) == '.'){
                        name = name.substring(0, A);
                        break;
                    }
                }
                if(filter != null && (!name.startsWith(filter))){
                    continue;
                }
                list.add(name);
            }
        });
        return list;
    }
}
