package org.ships.commands.legacy.shiptype;

import org.array.utils.ArrayUtils;
import org.core.TranslateCore;
import org.core.config.parser.StringParser;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.SwitchableVessel;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.assits.shiptype.SerializableShipType;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Deprecated
public class LegacyShipTypeCommand implements LegacyArgumentCommand {

    @Override
    public String getName() {
        return "shiptype";
    }

    @Override
    public Optional<String> getPermission() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandSource source, String... args) {
        if (args.length < 2) {
            return false;
        }
        if (args[1].equalsIgnoreCase("flags")) {
            Optional<ShipType> opType = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(t -> t.getId().equalsIgnoreCase(args[2])).findAny();
            if (!opType.isPresent()) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessagePlain("Unknown ShipType.");
                }
                return true;
            }
            ShipType<?> type = opType.get();
            Optional<VesselFlag<?>> opFlag = type.getFlags().stream().filter(f -> ((VesselFlag<?>) f).getId().equals(args[3])).findAny();
            if (!opFlag.isPresent()) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessagePlain("Unknown Flag for this ShipType.");
                }
                return true;
            }
            VesselFlag<?> flag = opFlag.get();
            if (args.length >= 5 && args[4].equalsIgnoreCase("set")) {
                if (args.length >= 6) {
                    if (!(flag.getParser() instanceof StringParser)) {
                        if (source instanceof CommandViewer) {
                            ((CommandViewer) source).sendMessagePlain("Flag does not accept String.");
                        }
                        return true;
                    }
                    if (updateFlag(flag, ArrayUtils.toString(" ", t -> t, ArrayUtils.filter(5, args.length, args)))) {
                        if (type instanceof SerializableShipType) {
                            ((SerializableShipType<?>) type).save();
                        }
                        if (source instanceof CommandViewer) {
                            ((CommandViewer) source).sendMessagePlain("Flag value updated");
                        }
                        return true;
                    }
                    if (source instanceof CommandViewer) {
                        ((CommandViewer) source).sendMessagePlain("Flag value could not update");
                    }
                }
                flag.setValue(null);
                if (type instanceof SerializableShipType) {
                    ((SerializableShipType<?>) type).save();
                }
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessagePlain("Flag value updated");
                }
                return true;
            }
            if (source instanceof CommandViewer) {
                ((CommandViewer) source).sendMessagePlain("/shiptype flags <vessel type> <flag> set <value>");
            }
            return true;
        }
        if (args[1].equalsIgnoreCase("create")) {
            if (source instanceof LivePlayer) {
                if (!((LivePlayer) source).hasPermission(Permissions.CMD_SHIPTYPE_CREATE)) {
                    ((LivePlayer) source).sendMessage(TranslateCore.buildText(TextColours.RED + "You do not have permission for that command"));
                }
            }
            if (args.length!=4) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessage(TranslateCore.buildText(TextColours.RED + "/ships shiptype create <cloneable ship type> <name of new ship type>"));
                }
                return false;
            }
            Optional<CloneableShipType> opType = ShipsPlugin.getPlugin().getAll(CloneableShipType.class).stream().filter(t -> t.getId().equalsIgnoreCase(args[2])).findAny();
            if (!opType.isPresent()) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessagePlain("Unknown ShipType. Must be cloneable");
                }
                return true;
            }
            for (int A = 3; A < args.length; A++) {
                File file = new File(ShipsPlugin.getPlugin().getConfigFolder(), "Configuration/ShipType/Custom/" + opType.get().getOriginType().getId().replace(":", ".") + "/" + args[A] + "." + TranslateCore.getPlatform().getConfigFormat().getFileType()[0]);
                file = TranslateCore.createConfigurationFile(file, TranslateCore.getPlatform().getConfigFormat()).getFile();
                if (file.exists()) {
                    if (source instanceof CommandViewer) {
                        ((CommandViewer) source).sendMessagePlain("Custom ShipType " + args[A] + " has already been created");
                    }
                    continue;
                }
                try {
                    file.getParentFile().mkdirs();
                    Files.copy(opType.get().getOriginType().getFile().getFile().toPath(), file.toPath());
                } catch (IOException e) {
                    if (source instanceof CommandViewer) {
                        ((CommandViewer) source).sendMessagePlain(args[A] + " failed to created file. " + e.getMessage());
                    }
                    e.printStackTrace();
                }
                CloneableShipType newType = opType.get().getOriginType().cloneWithName(file, args[A]);
                ShipsPlugin.getPlugin().register(newType);
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessagePlain(args[A] + " created. ");
                }
            }
            return true;
        }
        if (args[1].equalsIgnoreCase("view")) {
            if (source instanceof CommandViewer) {
                ((CommandViewer) source).sendMessagePlain(ArrayUtils.toString(", ", ShipType::getDisplayName, ShipsPlugin.getPlugin().getAll(ShipType.class)));
            }
            return true;
        }
        if (args[1].equalsIgnoreCase("delete")) {
            Optional<CloneableShipType> opType = ShipsPlugin.getPlugin().getAll(CloneableShipType.class).stream().filter(t -> t.getId().equalsIgnoreCase(args[2])).findAny();
            if (!opType.isPresent()) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessagePlain("Unknown ShipType. Must be cloneable");
                }
                return true;
            }
            Set<Vessel> vessels = ShipsPlugin.getPlugin().getVessels().stream().filter(v -> v.getType().equals(opType.get())).filter(v -> v instanceof SwitchableVessel).collect(Collectors.toSet());
            long count = vessels.stream().filter(v -> {
                try {
                    ((SwitchableVessel<CloneableShipType<?>>) v).setType((CloneableShipType<?>) opType.get());
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }).count();
            if (count!=vessels.size()) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessagePlain("Could not delete. Could not convert all vessels into " + opType.get().getOriginType().getId());
                }
                return true;
            }
            try {
                Files.delete(opType.get().getFile().getFile().toPath());
            } catch (IOException e) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessagePlain("Could not delete. " + e.getMessage());
                }
                e.printStackTrace();
                return true;
            }
            ShipsPlugin.getPlugin().unregister(opType.get());
            return true;
        }
        return false;
    }

    @Override
    public List<String> tab(CommandSource source, String... args) {
        if (args.length==2 && args[1].equalsIgnoreCase("")) {
            return Arrays.asList("delete", "view", "create", "flags");
        }
        if (args.length==2) {
            List<String> list = new ArrayList<>();
            if ("delete".startsWith(args[1].toLowerCase())) {
                list.add("delete");
            }
            if ("view".startsWith(args[1].toLowerCase())) {
                list.add("view");
            }
            if ("create".startsWith(args[1].toLowerCase())) {
                list.add("create");
            }
            if ("flags".startsWith(args[1].toLowerCase())) {
                list.add("flags");
            }
            return list;
        }
        if (args.length==3 && args[2].equalsIgnoreCase("") && args[1].equalsIgnoreCase("flags")) {
            List<String> ids = new ArrayList<>();
            ShipsPlugin.getPlugin().getAll(ShipType.class).forEach(v -> ids.add(v.getId()));
            return ids;
        }
        if (args.length==3 && args[1].equalsIgnoreCase("flags")) {
            List<String> ids = new ArrayList<>();
            ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(t -> t.getId().startsWith(args[2])).forEach(v -> ids.add(v.getId()));
            return ids;
        }
        if (args.length==3 && args[2].equalsIgnoreCase("") && args[1].equalsIgnoreCase("delete")) {
            List<String> ids = new ArrayList<>();
            ShipsPlugin.getPlugin().getAll(CloneableShipType.class).stream().filter(t -> !t.getOriginType().equals(t)).forEach(v -> ids.add(v.getId()));
            return ids;
        }
        if (args.length==3 && args[1].equalsIgnoreCase("delete")) {
            List<String> ids = new ArrayList<>();
            ShipsPlugin.getPlugin().getAll(CloneableShipType.class).stream().filter(t -> !t.getOriginType().equals(t)).filter(t -> t.getId().startsWith(args[2])).forEach(v -> ids.add(v.getId()));
            return ids;
        }
        if (args.length==3 && args[2].equalsIgnoreCase("") && args[1].equalsIgnoreCase("create")) {
            List<String> ids = new ArrayList<>();
            ShipsPlugin.getPlugin().getAll(CloneableShipType.class).stream().filter(t -> t.getOriginType().equals(t)).forEach(v -> ids.add(v.getId()));
            return ids;
        }
        if (args.length==3 && args[1].equalsIgnoreCase("create")) {
            List<String> ids = new ArrayList<>();
            ShipsPlugin.getPlugin().getAll(CloneableShipType.class).stream().filter(t -> t.getOriginType().equals(t)).filter(t -> t.getId().startsWith(args[2])).forEach(v -> ids.add(v.getId()));
            return ids;
        }
        if (args.length==4 && args[3].equalsIgnoreCase("") && args[1].equalsIgnoreCase("flags")) {
            Optional<ShipType> opType = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(f -> f.getId().equalsIgnoreCase(args[2])).findAny();
            if (!opType.isPresent()) {
                return new ArrayList<>();
            }
            List<String> ret = new ArrayList<>();
            opType.get().getFlags().stream().filter(f -> ((VesselFlag<?>) f).getParser() instanceof StringParser).forEach(f -> ret.add(((VesselFlag<?>) f).getId()));
            return ret;
        }
        if (args.length==4 && args[1].equalsIgnoreCase("flags")) {
            Optional<ShipType> opType = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(f -> f.getId().equalsIgnoreCase(args[2])).findAny();
            if (!opType.isPresent()) {
                return new ArrayList<>();
            }
            List<String> ret = new ArrayList<>();
            opType.get().getFlags().stream().filter(f -> ((VesselFlag<?>) f).getId().startsWith(args[3].toLowerCase())).filter(f -> ((VesselFlag<?>) f).getParser() instanceof StringParser).forEach(f -> ret.add(((VesselFlag<?>) f).getId()));
            return ret;
        }
        if (args.length==5 && args[1].equalsIgnoreCase("flags") && args[4].equalsIgnoreCase("")) {
            return Arrays.asList("set");
        }
        if (args.length==5 && args[1].equalsIgnoreCase("flags") && args[4].equalsIgnoreCase("")) {
            List<String> ret = new ArrayList<>();
            if ("set".startsWith(args[4])) {
                ret.add("set");
            }
            return ret;
        }
        if (args.length==6 && args[1].equalsIgnoreCase("flags")) {
            Optional<ShipType> opType = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(f -> f.getId().equalsIgnoreCase(args[2])).findAny();
            if (!opType.isPresent()) {
                return new ArrayList<>();
            }
            Optional<VesselFlag<?>> opFlag = opType.get().getFlags().stream().filter(f -> ((VesselFlag<?>) f).getId().equalsIgnoreCase(args[3])).findAny();
            if (!opFlag.isPresent()) {
                return new ArrayList<>();
            }
            if (opFlag.get().getParser() instanceof StringParser.Suggestible) {
                StringParser.Suggestible<?> parser = (StringParser.Suggestible<?>) opFlag.get().getParser();
                return parser.getStringSuggestions(args[5]);
            }
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    private <T> boolean updateFlag(VesselFlag<T> flag, String value) {
        Optional<T> opParsed = ((StringParser<T>) flag.getParser()).parse(value);
        if (opParsed.isPresent()) {
            flag.setValue(opParsed.get());
            return true;
        }
        return false;
    }
}
