package org.ships.commands.legacy.blocklist;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.core.world.position.block.BlockType;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.DefaultBlockList;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;

import java.util.*;
import java.util.stream.Stream;

@Deprecated
public class LegacyBlockListCommand implements LegacyArgumentCommand {

    @Override
    public String getName() {
        return "blocklist";
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
        CommandViewer viewer = (CommandViewer)source;
        List<BlockType> list = new ArrayList<>();
        if(args.length == 1) {
            list.addAll(TranslateCore.getPlatform().getBlockTypes());
        }else if(args[1].equalsIgnoreCase("set")) {
            if (source instanceof LivePlayer && !((LivePlayer)source).hasPermission(Permissions.CMD_BLOCKLIST_SET)){
                LivePlayer player = (LivePlayer)source;
                AdventureMessageConfig messageConfig = ShipsPlugin.getPlugin().getAdventureMessageConfig();
                AText text = AdventureMessageConfig.ERROR_PERMISSION_MISS_MATCH.process(new AbstractMap.SimpleImmutableEntry<>(player, Permissions.CMD_BLOCKLIST_SET.getPermissionValue()));
                player.sendMessage(text);
                return false;
            }
            if(args.length >= 3){
                if (args[2].equalsIgnoreCase("blocklimit") || args[2].equalsIgnoreCase("bl")){
                    if (args.length < 5) {
                            viewer.sendMessagePlain("/ships blocklist set blocklimit <amount> <blocks>");
                        return false;
                    }
                    try{
                        int limit = Integer.parseInt(args[3]);
                        if (limit < -1){
                            viewer.sendMessagePlain("Number must be greater then -1");
                            return false;
                        }
                        Collection<BlockType> blockTypes = TranslateCore.getPlatform().getBlockTypes();
                        for(int A = 3; A < args.length; A++){
                            int B = A;
                            Optional<BlockType> opType = blockTypes.stream().filter(b -> b.getId().startsWith(args[B]) || b.getId().split(":", 2)[1].startsWith(args[B])).findAny();
                            opType.ifPresent(list::add);
                        }
                        DefaultBlockList dbl = ShipsPlugin.getPlugin().getBlockList();
                        list.forEach(b -> dbl.replaceBlockInstruction(dbl.getBlockInstruction(b).setBlockLimit(limit)));
                        dbl.saveChanges();
                        viewer.sendMessage(TranslateCore.buildText(TextColours.AQUA.toString() + list.size() + " materials changed"));
                        return true;
                    }catch (NumberFormatException e){
                        viewer.sendMessagePlain("Unknown number of " + args[3]);
                        return false;
                    }
                }
                if(args[2].equalsIgnoreCase("collidetype") || args[2].equalsIgnoreCase("ct")) {
                    if (args.length < 5) {
                        viewer.sendMessagePlain("/ships blocklist set collidetype <collidetype> <blocks>");
                        return false;
                    }
                    BlockInstruction.CollideType type = Stream.of(BlockInstruction.CollideType.values()).filter(b -> b.name().equalsIgnoreCase(args[3])).findAny().orElse(null);
                    if(type == null){
                        viewer.sendMessagePlain("Unknown collide type of " + args[3]);
                        return false;
                    }
                    Collection<BlockType> blockTypes = TranslateCore.getPlatform().getBlockTypes();
                    for(int A = 3; A < args.length; A++){
                        int B = A;
                        Optional<BlockType> opType = blockTypes.stream().filter(b -> b.getId().equalsIgnoreCase(args[B]) || b.getId().split(":", 2)[1].equalsIgnoreCase(args[B])).findAny();
                        opType.ifPresent(list::add);
                    }
                    DefaultBlockList dbl = ShipsPlugin.getPlugin().getBlockList();
                    list.forEach(b -> dbl.replaceBlockInstruction(dbl.getBlockInstruction(b).setCollideType(type)));
                    dbl.saveChanges();
                    viewer.sendMessage(TranslateCore.buildText(TextColours.AQUA.toString() + list.size() + " materials changed"));
                    return true;
                }
            }

        }else if(args[1].equalsIgnoreCase("view")) {
            if (source instanceof LivePlayer && !((LivePlayer)source).hasPermission(Permissions.CMD_BLOCKLIST_VIEW)){
                ((LivePlayer) source).sendMessage(TranslateCore.buildText(TextColours.RED + "You do not have permission for that command"));
                return false;
            }
            Collection<BlockType> blockTypes = TranslateCore.getPlatform().getBlockTypes();
            if (args.length > 2) {
                for (int A = 2; A < args.length; A++) {
                    int B = A;
                    Optional<BlockType> opType = blockTypes.stream().filter(b -> b.getId().startsWith(args[B]) || b.getId().split(":", 2)[1].startsWith(args[B])).findAny();
                    opType.ifPresent(list::add);
                }
            } else {
                list.addAll(blockTypes);
            }
            Collection<BlockInstruction> blockList = ShipsPlugin.getPlugin().getBlockList().getBlockList();
            Set<BlockType> material = new HashSet<>();
            Set<BlockType> ignore = new HashSet<>();
            Set<BlockType> collide = new HashSet<>();
            list.forEach(l -> {
                BlockInstruction bi = blockList.stream().filter(b -> b.getType().equals(l)).findAny().get();
                switch (bi.getCollideType()) {
                    case DETECT_COLLIDE:
                        collide.add(l);
                        break;
                    case MATERIAL:
                        material.add(l);
                        break;
                    case IGNORE:
                        ignore.add(l);
                        break;
                }
            });
            viewer.sendMessage(TranslateCore.buildText(TextColours.RED + "|----{Blocks}----|"));
            viewer.sendMessage(TranslateCore.buildText(TextColours.GREEN + "Material: " + TextColours.AQUA + material.size()));
            viewer.sendMessage(TranslateCore.buildText(TextColours.GREEN + "Ignore: " + TextColours.AQUA + ignore.size()));
            viewer.sendMessage(TranslateCore.buildText(TextColours.GREEN + "Detect Collide: " + TextColours.AQUA + collide.size()));
        }
        return true;
    }

    @Override
    public List<String> tab(CommandSource source, String... args) {
        List<String> list = new ArrayList<>();
        if(args.length == 2 && args[1].equalsIgnoreCase("")) {
            list.add("view");
            list.add("set");
            return list;
        } else if(args.length == 2) {
            if ("view".startsWith(args[1].toLowerCase())) {
                list.add("view");
            }
            if ("set".startsWith(args[1].toLowerCase())) {
                list.add("set");
            }
            return list;
        } else if(args[1].equalsIgnoreCase("set") && args.length == 3) {
            if (args[2].equalsIgnoreCase("")) {
                list.add("collidetype");
                list.add("blocklimit");
                return list;
            } else {
                if ("collidetype".startsWith(args[2])) {
                    list.add("collidetype");
                }
                if ("blocklimit".startsWith(args[2])){
                    list.add("blocklimit");
                }
                return list;
            }
        } else if(args[1].equalsIgnoreCase("set") && args.length == 4 && (args[2].equalsIgnoreCase("collidetype") || args[2].equalsIgnoreCase("ct"))) {
            if (args[3].equalsIgnoreCase("")) {
                Stream.of(BlockInstruction.CollideType.values()).forEach(c -> {
                    list.add(c.name().toLowerCase());
                });
            } else {
                Stream.of(BlockInstruction.CollideType.values()).filter(c -> c.name().startsWith(args[3].toUpperCase())).forEach(c -> {
                    list.add(c.name().toLowerCase());
                });
            }
            return list;
        }else if(args[1].equalsIgnoreCase("set") && args.length == 4 && (args[2].equalsIgnoreCase("blocklimit") || args[2].equalsIgnoreCase("bl"))){
            if (args[3].equals("-")){
                list.add("-1");
            }
        } else {
            String compare = args[args.length - 1];
            TranslateCore.getPlatform().getBlockTypes().stream().filter(b -> {
                if (b.getId().startsWith(compare)) {
                    return true;
                }
                return b.getId().split(":", 2)[1].startsWith(compare);
            }).forEach(b -> list.add(b.getId()));
            if (list.isEmpty()) {
                TranslateCore.getPlatform().getBlockTypes().forEach(b -> list.add(b.getId()));
            }
        }
        return list;
    }
}
