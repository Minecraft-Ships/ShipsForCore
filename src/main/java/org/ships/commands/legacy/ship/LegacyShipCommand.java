package org.ships.commands.legacy.ship;

import org.core.CorePlugin;
import org.core.configuration.parser.Parser;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.User;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.core.utils.Identifable;
import org.core.world.position.BlockPosition;
import org.core.world.position.ExactPosition;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.movement.autopilot.scheduler.EOTExecutor;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.assits.TeleportToVessel;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.loader.ShipsIDFinder;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.sign.EOTSign;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LegacyShipCommand implements LegacyArgumentCommand {

    @Override
    public String getName() {
        return "ship";
    }

    @Override
    public Optional<String> getPermission() {
        return Optional.empty();
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
            if(args[2].equalsIgnoreCase("track")) {
                if (!(source instanceof LivePlayer)) {
                    if (source instanceof CommandViewer) {
                        ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + "Player only command"));
                    }
                    return true;
                }
                LivePlayer player = (LivePlayer) source;
                if (!player.hasPermission(Permissions.CMD_SHIP_TRACK)) {
                    return false;
                }
                vessel.getStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.OBSIDIAN.get().getDefaultBlockDetails(), (LivePlayer) source));
                CorePlugin.createSchedulerBuilder()
                        .setDisplayName("ShipsTrack:" + vessel.getName())
                        .setDelay(10)
                        .setDelayUnit(TimeUnit.SECONDS)
                        .setExecutor(() -> vessel
                                .getStructure()
                                .getPositions()
                                .forEach(bp -> bp.resetBlock(player)))
                        .build(ShipsPlugin.getPlugin())
                        .run();
            }else if(args[2].equalsIgnoreCase("Teleport")){
                if(!(source instanceof LivePlayer)){
                    if(source instanceof CommandViewer){
                        ((CommandViewer)source).sendMessagePlain("Teleport requires to be ran as a player");
                    }
                    return false;
                }
                LivePlayer player = (LivePlayer)source;
                TeleportToVessel tVessel = (TeleportToVessel) vessel;
                if(args.length == 3){
                    ExactPosition pos = tVessel.getTeleportPositions().getOrDefault("Default", tVessel.getPosition().toExactPosition());
                    player.setPosition(pos);
                    return true;
                }
                if(args.length >= 5 && args[3].equalsIgnoreCase("set")){
                    tVessel.getTeleportPositions().put(args[4], player.getPosition());
                    tVessel.save();
                    return true;
                }else if(args[3].equalsIgnoreCase("set")){
                    tVessel.getTeleportPositions().put("Default", player.getPosition());
                    tVessel.save();
                    return true;
                }
                ExactPosition position = tVessel.getTeleportPositions().get(args[3]);
                if(position == null){
                    player.sendMessagePlain("Unknown position on the ship");
                    return true;
                }
                player.setPosition(position);
                return true;
            }else if(args[2].equalsIgnoreCase("EOT")){
                if(args.length >= 5) {
                    if(args[3].equalsIgnoreCase("enable")){
                        Optional<Boolean> opCheck = Parser.STRING_TO_BOOLEAN.parse(args[4]);
                        if(!opCheck.isPresent()){
                            if(source instanceof CommandViewer){
                                ((CommandViewer)source).sendMessagePlain("/ships ship " + args[1] + " eot enable <true/false>");
                            }
                            return false;
                        }
                        EOTSign sign = ShipsPlugin.getPlugin().get(EOTSign.class).get();
                        if(!opCheck.get()){
                            sign.getScheduler(vessel).forEach(s -> {
                                EOTExecutor exe = (EOTExecutor) s.getExecutor();
                                exe.getSign().ifPresent(b -> {
                                    Optional<LiveTileEntity> opTileEntity = b.getTileEntity();
                                    if(!opTileEntity.isPresent()){
                                        return;
                                    }
                                    if (!(opTileEntity.get() instanceof LiveSignTileEntity)){
                                        return;
                                    }
                                    LiveSignTileEntity lste = (LiveSignTileEntity)opTileEntity.get();
                                    lste.setLine(1, CorePlugin.buildText("Ahead"));
                                    lste.setLine(2, CorePlugin.buildText("{Stop}"));
                                });
                                s.cancel();
                            });
                        }
                        Collection<BlockPosition> eotSigns = vessel.getStructure().getAll(sign);
                        if(eotSigns.size() == 1 && opCheck.get()){
                            if(!(source instanceof LivePlayer)){
                                if(source instanceof CommandViewer){
                                    ((CommandViewer) source).sendMessagePlain("Can only enable eot as a player");
                                }
                                return false;
                            }
                            LivePlayer player = (LivePlayer)source;
                            LiveSignTileEntity lste = (LiveSignTileEntity) eotSigns.stream().findAny().get().getTileEntity().get();
                            sign.onSecondClick(player, lste.getPosition());
                        }else if(opCheck.get()){
                            if(source instanceof CommandViewer){
                                ((CommandViewer) source).sendMessagePlain("Found more then one EOT sign, unable to enable.");
                            }
                            return false;
                        }
                    }
                }else{
                    if(source instanceof CommandViewer){
                        ((CommandViewer)source).sendMessagePlain("/ships ship " + args[1] + " eot enable <true/false>");
                    }
                }
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
            ShipsPlugin.getPlugin().getVessels().stream().filter(v -> v instanceof Identifable).forEach(v -> list.add(((Identifable) v).getId()));
        }else if(args.length == 2){
            ShipsPlugin.getPlugin().getVessels().stream().filter(v -> v instanceof Identifable).filter(v -> ((Identifable) v).getId().startsWith(args[1])).forEach(v -> list.add(((Identifable) v).getId()));
        }else if (args.length == 3 && args[2].equalsIgnoreCase("")){
            list.add("track");
            list.add("crew");
            list.add("info");
            list.add("teleport");
        }else if (args.length == 3) {
            if ("track".startsWith(args[2].toLowerCase())) {
                list.add("track");
            }
            if ("info".startsWith(args[2].toLowerCase())) {
                list.add("info");
            }
            if ("crew".startsWith(args[2].toLowerCase())) {
                list.add("crew");
            }
            if ("teleport".startsWith(args[2].toLowerCase())) {
                list.add("teleport");
            }
        }else if (args.length == 4 && args[2].equalsIgnoreCase("teleport") && args[3].equalsIgnoreCase("")) {
            list.add("set");
            try {
                Vessel vessel = new ShipsIDFinder(args[1]).load();
                if(vessel instanceof TeleportToVessel){
                    list.addAll(((TeleportToVessel) vessel).getTeleportPositions().keySet());
                }
            } catch (LoadVesselException e) {
            }
        }else if(args.length == 4 && args[2].equalsIgnoreCase("teleport")) {
            if ("set".startsWith(args[3])) {
                list.add("set");
            }
            try {
                Vessel vessel = new ShipsIDFinder(args[1]).load();
                if (vessel instanceof TeleportToVessel) {
                    list.addAll(((TeleportToVessel) vessel).getTeleportPositions().keySet().stream().filter(id -> id.startsWith(args[3])).collect(Collectors.toSet()));
                }
            } catch (LoadVesselException e) {
            }
        }else if(args.length == 5 && args[2].equalsIgnoreCase("teleport") && args[3].equalsIgnoreCase("set") && args[4].equalsIgnoreCase("")){
            try{
                Vessel vessel = new ShipsIDFinder(args[1]).load();
                if (vessel instanceof TeleportToVessel) {
                    list.addAll(((TeleportToVessel) vessel).getTeleportPositions().keySet());
                }
            } catch (LoadVesselException e) {
            }
        }else if(args.length == 5 && args[2].equalsIgnoreCase("teleport") && args[3].equalsIgnoreCase("set")){
            try{
                Vessel vessel = new ShipsIDFinder(args[1]).load();
                if (vessel instanceof TeleportToVessel) {
                    list.addAll(((TeleportToVessel) vessel).getTeleportPositions().keySet().stream().filter(id -> id.startsWith(args[4])).collect(Collectors.toSet()));
                }
            } catch (LoadVesselException e) {
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
