package org.ships.commands.legacy.autopilot;

import org.core.CorePlugin;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.core.world.position.BlockPosition;
import org.core.world.position.Positionable;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.movement.autopilot.BasicFlightPath;
import org.ships.movement.autopilot.scheduler.FlightPathExecutor;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.FlightPathType;
import org.ships.vessel.common.loader.ShipsIDFinder;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class LegacyAutoPilotCommand implements LegacyArgumentCommand {
    @Override
    public String getName() {
        return "autopilot";
    }

    @Override
    public Optional<String> getPermission() {
        return Optional.of(Permissions.CMD_AUTOPILOT);
    }

    @Override
    public boolean run(CommandSource source, String... args) {
        if(args.length <= 2){
            return false;
        }
        if(args[1].equalsIgnoreCase("deploy")) {
            if(args.length < 6){
                if(source instanceof CommandViewer){
                    ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + "autopilot deploy <vessel id> <x> <y> <z>"));
                }
                return false;
            }
            int x;
            int y;
            int z;
            try{
                x = Integer.parseInt(args[3]);
                y = Integer.parseInt(args[4]);
                z = Integer.parseInt(args[5]);
            }catch (NumberFormatException e){
                if(source instanceof CommandViewer){
                    ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + "x y z are not whole numbers"));
                }
                return false;
            }
            Vessel vessel;
            try {
                vessel = new ShipsIDFinder(args[2]).load();
            } catch (IOException e) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
                }
                return false;
            }
            if (!(vessel instanceof FlightPathType)) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + "Ship is not auto pilotable"));
                }
                return false;
            }
            FlightPathType vesselF = (FlightPathType) vessel;
            BlockPosition position = vesselF.getPosition().getWorld().getPosition(x, y, z);
            vesselF.setFlightPath(new BasicFlightPath(vessel.getPosition().getPosition(), position.getPosition()));
            CorePlugin
                    .createSchedulerBuilder()
                    .setIteration(5)
                    .setIterationUnit(TimeUnit.SECONDS)
                    .setExecutor(new FlightPathExecutor(vesselF))
                    .build(ShipsPlugin.getPlugin()).run();
        }else if(args[1].equalsIgnoreCase("Cancel")){
            if(args.length <= 3){
                if(source instanceof CommandViewer){
                    ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + "autopilot cancel <vessel id>"));
                }
                return false;
            }
            Vessel vessel;
            try {
                vessel = new ShipsIDFinder(args[2]).load();
            } catch (IOException e) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
                }
                return false;
            }
            if (!(vessel instanceof FlightPathType)) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + "Ship is not auto pilotable"));
                }
                return false;
            }
            FlightPathType vesselF = (FlightPathType) vessel;
            vesselF.setFlightPath(null);
        }
        return false;
    }

    @Override
    public List<String> tab(CommandSource source, String... args) {
        List<String> list = new ArrayList<>();
        if (args.length == 2 && args[1].equalsIgnoreCase("")){
            list.add("deploy");
            list.add("cancel");
        }else if(args.length == 2){
            if("deploy".startsWith(args[1].toLowerCase())){
                list.add("deploy");
            }
            if("cancel".startsWith(args[1].toLowerCase())){
                list.add("cancel");
            }
        }else if(args.length == 3 && args[2].equalsIgnoreCase("")){
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
        }else if (args.length == 3){
            ShipsPlugin.getPlugin().getAll(ShipType.class).forEach(v -> {
                File folder = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "VesselData/ships." + v.getName());
                File[] files = folder.listFiles();
                if(files != null){
                    for(File file : files){
                        if(file.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                            String[] nameSplit = file.getName().split(Pattern.quote("."));
                            nameSplit[nameSplit.length - 1] = "";
                            String name = v.getName().toLowerCase() + ":" + CorePlugin.toString(".", t -> t, nameSplit);
                            name = name.substring(0, name.length() - 1).toLowerCase();
                            list.add(name);
                        }
                    }
                }
            });
        }else if(args.length == 4 && source instanceof Positionable){
            list.add(((Positionable) source).getPosition().getX().intValue() + "");
        }else if(args.length == 5 && source instanceof Positionable){
            list.add(((Positionable) source).getPosition().getY().intValue() + "");
        }else if(args.length == 6 && source instanceof Positionable){
            list.add(((Positionable) source).getPosition().getZ().intValue() + "");
        }
        return list;
    }
}
