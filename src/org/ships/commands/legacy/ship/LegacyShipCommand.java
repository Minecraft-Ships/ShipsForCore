package org.ships.commands.legacy.ship;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.core.world.position.block.BlockTypes;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsIDLoader;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipsVessel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class LegacyShipCommand implements LegacyArgumentCommand {

    @Override
    public String getName() {
        return "ship";
    }

    @Override
    public boolean run(CommandSource source, String... args) {
        if(args.length < 3){
            if (source instanceof CommandViewer){
                ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + "/ships ship <ship type>"));
            }
            return true;
        }else if(args.length == 3){
            try {
                ShipsVessel vessel = new ShipsIDLoader(args[1]).load();
                if(args[2].equalsIgnoreCase("track")){
                    if(!(source instanceof LivePlayer)){
                        if(source instanceof CommandViewer){
                            ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + "Player only command"));
                        }
                        return true;
                    }
                    vessel.getStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.OBSIDIAN.get().getDefaultBlockDetails(), (LivePlayer)source));
                    CorePlugin.createSchedulerBuilder()
                            .setDelay(10)
                            .setDelayUnit(TimeUnit.SECONDS)
                            .setExecutor(() -> vessel
                                    .getStructure()
                                    .getPositions()
                                    .forEach(bp -> bp.resetBlock((LivePlayer) source)))
                            .build(ShipsPlugin.getPlugin())
                            .run();
                }
            } catch (IOException e) {
                if(source instanceof CommandViewer){
                    ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> tab(CommandSource source, String... args) {
        List<String> list = new ArrayList<>();
        if(args.length == 2 && args[1].equals("")) {
            ShipsPlugin.getPlugin().getAll(ShipType.class).forEach(v -> {
                File folder = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "VesselData/ships." + v.getName().toLowerCase());
                File[] files = folder.listFiles();
                if(files != null){
                    for(File file : files){
                        System.out.println("\t" + file.getAbsolutePath() + " | " + file.getName());
                        String[] nameSplit = file.getName().split(Pattern.quote("."));
                        nameSplit[nameSplit.length - 1] = "";
                        String name = v.getName().toLowerCase() + ":" + CorePlugin.toString(".", t -> t, nameSplit);
                        name = name.substring(0, name.length() - 1).toLowerCase();
                        list.add(name);
                    }
                }
            });
        }else if(args.length == 2){
            ShipsPlugin.getPlugin().getAll(ShipType.class).forEach(v -> {
                File folder = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "VesselData/ships." + v.getName());
                File[] files = folder.listFiles();
                if(files != null){
                    for(File file : files){
                        if(file.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            String[] nameSplit = file.getName().split(Pattern.quote("."));
                            nameSplit[nameSplit.length - 1] = "";
                            String name = v.getName().toLowerCase() + ":" + CorePlugin.toString(".", t -> t, nameSplit);
                            name = name.substring(0, name.length() - 1).toLowerCase();
                            list.add(name);
                        }
                    }
                }
            });
        }else if (args.length == 3 && args[2].equalsIgnoreCase("")){
            list.add("track");
        }else if (args.length == 3){
            if("track".startsWith(args[2].toLowerCase())){
                list.add("track");
            }
        }

        return list;
    }
}
