package org.ships.commands.legacy.ship;

import org.array.utils.ArrayUtils;
import org.core.CorePlugin;
import org.core.config.parser.Parser;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.User;
import org.core.schedule.unit.TimeUnit;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.core.utils.Identifable;
import org.core.world.position.Positionable;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncExactPosition;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.MoveException;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.SetMovingBlock;
import org.ships.movement.autopilot.BasicFlightPath;
import org.ships.movement.autopilot.scheduler.EOTExecutor;
import org.ships.movement.autopilot.scheduler.FlightPathExecutor;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.*;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.loader.ShipsIDFinder;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.sign.EOTSign;
import org.ships.vessel.sign.ShipsSign;

import java.io.IOException;
import java.util.*;
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
                return runTrack(source, vessel);
            }else if(args[2].equalsIgnoreCase("Teleport")){
                return runTeleport(source, vessel, args);
            }else if(args[2].equalsIgnoreCase("EOT")){
                return runEOT(source, vessel, args);
            }else if(args[2].equalsIgnoreCase("crew")){
                return runCrew(source, vessel, args);
            }else if(args[2].equalsIgnoreCase("info")){
                return runInfo(source, vessel, args);
            }else if(args[2].equalsIgnoreCase("autopilot")){
                return runAutoPilot(source, vessel, args);
            }else if(args[2].equalsIgnoreCase("unlock")){
                return runUnlock(source, vessel, args);
            }else if(args[2].equalsIgnoreCase("check")){
                return runCheck(source, vessel, args);
            }
        } catch (IOException e) {
            if(source instanceof CommandViewer){
                ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
            }
            return true;
        }
        return false;
    }

    private boolean runCheck(CommandSource source, Vessel vessel, String... args){
        if(!(source instanceof CommandViewer)){
            return false;
        }
        CommandViewer viewer = (CommandViewer)source;
        if(vessel instanceof Fallable){
            Fallable fVessel = (Fallable)vessel;
            viewer.sendMessagePlain("Will Fall: " + fVessel.shouldFall());
        }
        if(vessel instanceof VesselRequirement) {
            VesselRequirement rVessel = (VesselRequirement) vessel;
            MovementContext context = new MovementContext();
            MovingBlockSet set = new MovingBlockSet();
            for (SyncBlockPosition position : rVessel.getStructure().getPositions()) {
                set.add(new SetMovingBlock(position, position));
            }
            context.setMovingStructure(set);
            context.setStrictMovement(true);
            try {
                rVessel.meetsRequirements(context);
                viewer.sendMessagePlain("Meets Requirements: true");
            } catch (MoveException e) {
                viewer.sendMessagePlain("Meets Requirements: False");
                e.getMovement().sendMessage(viewer);
            }
        }
        return true;
    }

    private boolean runUnlock(CommandSource source, Vessel vessel, String... args){
        Set<SyncBlockPosition> set = vessel.getStructure().getPositions().stream().filter(p -> ShipsSign.LOCKED_SIGNS.stream().anyMatch(p1 -> p1.equals(p))).collect(Collectors.toSet());
        if(set.isEmpty()){
            if(source instanceof CommandViewer){
                ((CommandViewer) source).sendMessagePlain("Cleared all locked signs");
            }
            ShipsSign.LOCKED_SIGNS.clear();
            return true;
        }
        if(source instanceof CommandViewer){
            ((CommandViewer) source).sendMessagePlain("Cleared all (" + set.size() + ") locked signs");
        }
        set.forEach(ShipsSign.LOCKED_SIGNS::remove);
        return true;
    }

    private boolean runAutoPilot(CommandSource source, Vessel vessel, String... args){
        if(args[3].equalsIgnoreCase("deploy")){
            if(args.length < 7){
                if(!(source instanceof CommandViewer)){
                    return false;
                }
                CommandViewer viewer = (CommandViewer)source;
                viewer.sendMessagePlain("/ships ship " + args[1] + " autopilot <deploy/cancel> <X> <Y> <Z>");
                return false;
            }
            int x;
            int y;
            int z;
            try {
                x = Integer.parseInt(args[4]);
                y = Integer.parseInt(args[5]);
                z = Integer.parseInt(args[6]);
            }catch (NumberFormatException e){
                if(source instanceof CommandViewer){
                    ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + "x y z are not whole numbers"));
                }
                return false;
            }
            if(!(vessel instanceof FlightPathType)){
                if(source instanceof CommandViewer){
                    ((CommandViewer)source).sendMessage(CorePlugin.buildText(TextColours.RED + vessel.getType().getId() + " is not allowed to be auto piloted"));
                }
                return false;
            }
            FlightPathType flightVessel = (FlightPathType)vessel;
            BlockPosition position = flightVessel.getPosition().getWorld().getPosition(x, y, z);
            BasicFlightPath bfp = new BasicFlightPath(vessel.getPosition().getPosition(), position.getPosition());
            if(source instanceof CommandViewer) {
                bfp.setViewer((CommandViewer)source);
            }
            flightVessel.setFlightPath(bfp);
            CorePlugin
                    .createSchedulerBuilder()
                    .setIteration(5)
                    .setIterationUnit(TimeUnit.SECONDS)
                    .setExecutor(new FlightPathExecutor(flightVessel))
                    .setDisplayName("AutoPilot")
                    .build(ShipsPlugin.getPlugin()).run();
            return true;
        }else if(args[3].equalsIgnoreCase("cancel")) {
            if(!(vessel instanceof FlightPathType)){
                if(source instanceof CommandViewer){
                    ((CommandViewer)source).sendMessage(CorePlugin.buildText(TextColours.RED + vessel.getType().getId() + " is not allowed to be auto piloted"));
                }
                return false;
            }
            FlightPathType flightVessel = (FlightPathType)vessel;
            flightVessel.setFlightPath(null);
            return true;
        }
        if(source instanceof CommandViewer){
            CommandViewer viewer = (CommandViewer)source;
            viewer.sendMessagePlain(args[3] + " Unknown command");
            viewer.sendMessagePlain("/ships ship " + args[1] + "<deploy/cancel> <x> <y> <z>");
        }
        return false;
    }

    private boolean runInfo(CommandSource source, Vessel vessel, String... args){
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
        if(vessel instanceof ShipsVessel) {
            viewer.sendMessagePlain("Flags:");
            viewer.sendMessagePlain(" - " + ArrayUtils.toString("\n - ", f -> {
                if (f instanceof VesselFlag.Serializable) {
                    return ((VesselFlag.Serializable<?>) f).serialize();
                }
                return "";
            }, ((ShipsVessel)vessel).getFlags()));
        }
        viewer.sendMessagePlain("Entities: ");
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        vessel.getEntitiesOvertime(config.getEntityTrackingLimit(), e -> true, e -> {
            String entity = null;
            if (e instanceof LivePlayer) {
                LivePlayer player = (LivePlayer) e;
                entity = "player: " + player.getName();
            } else {
                entity = e.getType().getName();
            }
            viewer.sendMessagePlain("- " + entity);
        }, e -> {});
        return true;
    }

    private boolean runCrew(CommandSource source, Vessel vessel, String... args){
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
        return false;
    }

    private boolean runEOT(CommandSource source, Vessel vessel, String... args){
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
                    return true;
                }
                Collection<SyncBlockPosition> eotSigns = vessel.getStructure().getAll(sign);
                if(eotSigns.size() == 1){
                    if(!(source instanceof LivePlayer)){
                        if(source instanceof CommandViewer){
                            ((CommandViewer) source).sendMessagePlain("Can only enable eot as a player");
                        }
                        return false;
                    }
                    LivePlayer player = (LivePlayer)source;
                    LiveSignTileEntity lste = (LiveSignTileEntity) eotSigns.stream().findAny().get().getTileEntity().get();
                    sign.onSecondClick(player, lste.getPosition());
                }else{
                    if(source instanceof CommandViewer){
                        ((CommandViewer) source).sendMessagePlain("Found more then one EOT sign, unable to enable.");
                    }
                }
                return false;
            }
        }else{
            if(source instanceof CommandViewer){
                ((CommandViewer)source).sendMessagePlain("/ships ship " + args[1] + " eot enable <true/false>");
            }
            return false;
        }
        return false;
    }

    private boolean runTeleport(CommandSource source, Vessel vessel, String... args){
        if(!(source instanceof LivePlayer)){
            if(source instanceof CommandViewer){
                ((CommandViewer)source).sendMessagePlain("Teleport requires to be ran as a player");
            }
            return false;
        }
        LivePlayer player = (LivePlayer)source;
        TeleportToVessel tVessel = (TeleportToVessel) vessel;
        if(args.length == 3){
            SyncExactPosition pos = tVessel.getTeleportPositions().getOrDefault("Default", tVessel.getPosition().toExactPosition());
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
        SyncExactPosition position = tVessel.getTeleportPositions().get(args[3]);
        if(position == null){
            player.sendMessagePlain("Unknown position on the ship");
            return true;
        }
        player.setPosition(position);
        return true;
    }

    private boolean runTrack(CommandSource source, Vessel vessel){
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
        return true;
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
            list.add("autopilot");
            list.add("unlock");
            list.add("check");
        }else if (args.length == 3) {
            if ("autopilot".startsWith(args[2].toLowerCase())) {
                list.add("autopilot");
            }
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
            if ("unlock".startsWith(args[2].toLowerCase())) {
                list.add("unlock");
            }
            if ("check".startsWith(args[2].toLowerCase())) {
                list.add("check");
            }
        }else if (args.length == 4 && args[2].equalsIgnoreCase("autopilot") && args[3].equalsIgnoreCase("")){
            list.add("deploy");
            list.add("cancel");
        }else if (args.length == 4 && args[2].equalsIgnoreCase("autopilot")){
            if("deploy".startsWith(args[3].toLowerCase())){
                list.add("deploy");
            }
            if("cancel".startsWith(args[3].toLowerCase())){
                list.add("cancel");
            }
        }else if (args.length == 5 && args[2].equalsIgnoreCase("autopilot") && args[3].equalsIgnoreCase("deploy")){
            if(source instanceof Positionable){
                list.add(((Positionable)source).getPosition().getX().intValue() + "");
            }
        }else if (args.length == 6 && args[2].equalsIgnoreCase("autopilot") && args[3].equalsIgnoreCase("deploy")){
            if(source instanceof Positionable){
                list.add(((Positionable)source).getPosition().getY().intValue() + "");
            }
        }else if (args.length == 7 && args[2].equalsIgnoreCase("autopilot") && args[3].equalsIgnoreCase("deploy")){
            if(source instanceof Positionable){
                list.add(((Positionable)source).getPosition().getZ().intValue() + "");
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
