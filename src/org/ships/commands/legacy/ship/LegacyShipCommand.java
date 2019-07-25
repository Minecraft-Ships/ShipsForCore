package org.ships.commands.legacy.ship;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.User;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.core.utils.Identifable;
import org.core.world.position.block.BlockTypes;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.loader.ShipsIDFinder;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.ShipsVessel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
        }
        try {
            Vessel vessel = new ShipsIDFinder(args[1]).load();
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
            }else if(args[2].equalsIgnoreCase("crew")){
                if(args.length == 3){
                    if(source instanceof CommandViewer){
                        ((CommandViewer)source).sendMessagePlain("/ships ship " + args[1] + " crew <set/view> <permission>");
                    }
                    return false;
                }else if(args.length >= 5){
                    String type = args[3];
                    if(vessel instanceof CrewStoredVessel) {
                        Set<User> crew = ((CrewStoredVessel)vessel).getUserCrew(args[4]);
                        if (type.equalsIgnoreCase("view")) {
                            if (!(source instanceof CommandViewer)) {
                                return false;
                            }
                            CommandViewer viewer = (CommandViewer) source;
                            viewer.sendMessagePlain("|----[" + args[4] + "]----|");
                            crew.forEach(u -> viewer.sendMessagePlain(" |- " + u.getName()));
                        }
                    }
                }
            }else if(args[2].equalsIgnoreCase("info")){
                if(!(source instanceof CommandViewer)){
                    return false;
                }
                CommandViewer viewer = (CommandViewer) source;
                viewer.sendMessagePlain("Name: " + vessel.getName());
                if(vessel instanceof Identifable) {
                    viewer.sendMessagePlain("ID: " + ((Identifable)vessel).getId());
                }
                viewer.sendMessagePlain("Max Speed: " + vessel.getMaxSpeed());
                viewer.sendMessagePlain("Altitude Speed: " + vessel.getAltitudeSpeed());
                viewer.sendMessagePlain("Size: " + vessel.getStructure().getPositions().size());
                if(vessel instanceof CrewStoredVessel) {
                    viewer.sendMessagePlain("Default Permission: " + ((CrewStoredVessel) vessel).getDefaultPermission().getId());
                }
                if(vessel instanceof ShipsVessel) {
                    ((ShipsVessel) vessel).getExtraInformation().forEach((key, value) -> viewer.sendMessagePlain(key + ": " + value));
                }
                viewer.sendMessagePlain("Entities: ");
                viewer.sendMessagePlain(" - " + CorePlugin.toString("\n - ", e -> {
                    if(e instanceof LivePlayer){
                        LivePlayer player = (LivePlayer)e;
                        return "player: " + player.getName();
                    }
                    return e.getType().getName();
                }, vessel.getEntities()));
                if(vessel instanceof ShipsVessel) {
                    viewer.sendMessagePlain("Flags:");
                    viewer.sendMessagePlain(" - " + CorePlugin.toString("\n - ", f -> {
                        if (f instanceof VesselFlag.Serializable) {
                            return ((VesselFlag.Serializable<?>) f).serialize();
                        }
                        return "";
                    }, ((ShipsVessel)vessel).getFlags()));
                }
            }
        } catch (IOException e) {
            if(source instanceof CommandViewer){
                ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
            }
            return true;
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
            list.add("crew");
            list.add("info");
        }else if (args.length == 3){
            if("track".startsWith(args[2].toLowerCase())){
                list.add("track");
            }
            if("info".startsWith(args[2].toLowerCase())){
                list.add("info");
            }
            if("crew".startsWith(args[2].toLowerCase())){
                list.add("crew");
            }
        }else if (args.length == 4 && args[2].equalsIgnoreCase("crew") && args[3].equalsIgnoreCase("")){
            list.add("view");
        }else if(args.length == 4 && args[2].equalsIgnoreCase("crew")){
            if("view".startsWith(args[3])){
                list.add("view");
            }
        }else if(args.length == 5 && args[2].equalsIgnoreCase("crew") && args[3].equalsIgnoreCase("")){
            try {
                Vessel vessel = new ShipsIDFinder(args[1]).load();
                if(vessel instanceof CrewStoredVessel) {
                    ((CrewStoredVessel)vessel).getCrew().values().forEach(p -> {
                        if (list.contains(p.getId())) {
                            return;
                        }
                        list.add(p.getId());
                    });
                }
            } catch (LoadVesselException e) {
                return list;
            }
        }else if(args.length == 5 && args[2].equalsIgnoreCase("crew")){
            try {
                Vessel vessel = new ShipsIDFinder(args[1]).load();
                if(vessel instanceof CrewStoredVessel) {
                    ((CrewStoredVessel)vessel).getCrew().values().forEach(p -> {
                        if (p.getId().toLowerCase().startsWith(args[4].toLowerCase())) {
                            if (list.contains(p.getId())) {
                                return;
                            }
                            list.add(p.getId());
                        }
                    });
                }
            } catch (LoadVesselException e) {
                return list;
            }
        }

        return list;
    }
}
