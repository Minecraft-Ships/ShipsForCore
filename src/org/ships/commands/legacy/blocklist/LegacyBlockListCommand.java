package org.ships.commands.legacy.blocklist;

import org.core.CorePlugin;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.core.world.position.block.BlockType;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.config.blocks.BlockInstruction;
import org.ships.plugin.ShipsPlugin;

import java.util.*;

public class LegacyBlockListCommand implements LegacyArgumentCommand {

    @Override
    public String getName() {
        return "BlockList";
    }

    @Override
    public boolean run(CommandSource source, String... args) {
        if(!(source instanceof CommandViewer)){
            return false;
        }
        List<BlockType> list = new ArrayList<>();
        if(args.length == 1) {
            list.addAll(CorePlugin.getPlatform().getBlockTypes());
        }else if(args[1].equalsIgnoreCase("set")) {
            /*if(args.length < 4){
                if(source instanceof CommandViewer){
                    ((CommandViewer)source).sendMessagePlain("/ships blocklist set <collidetype> <blocks>");
                }
                return false;
            }
            BlockInstruction.CollideType type = BlockInstruction.CollideType.valueOf(args[2]);
            if(type == null){
                if(source instanceof CommandViewer){
                    ((CommandViewer)source).sendMessagePlain("Unknown collide type of " + args[2]);
                }
                return false;
            }
            Collection<BlockType> blockTypes = CorePlugin.getPlatform().getBlockTypes();
            for(int A = 3; A < args.length; A++){
                int B = A;
                Optional<BlockType> opType = blockTypes.stream().filter(b -> b.getId().startsWith(args[B]) || b.getId().split(":", 2)[1].startsWith(args[B])).findAny();
                if(opType.isPresent()){
                    list.add(opType.get());
                }
            }
            DefaultBlockList blockList = ShipsPlugin.getPlugin().getBlockList();
            list.stream().forEach(f -> blockList.getBlockList().add(new BlockInstruction(f)));
            blockList.getFile().*/

        }else if(args[1].equalsIgnoreCase("view")){
            Collection<BlockType> blockTypes = CorePlugin.getPlatform().getBlockTypes();
            if(args.length > 2) {
                for (int A = 2; A < args.length; A++) {
                    int B = A;
                    Optional<BlockType> opType = blockTypes.stream().filter(b -> b.getId().startsWith(args[B]) || b.getId().split(":", 2)[1].startsWith(args[B])).findAny();
                    opType.ifPresent(list::add);
                }
            }else{
                list.addAll(blockTypes);
            }
        }
        Set<BlockInstruction> blockList = ShipsPlugin.getPlugin().getBlockList().getBlockList();
        Set<BlockType> material = new HashSet<>();
        Set<BlockType> ignore = new HashSet<>();
        Set<BlockType> collide = new HashSet<>();
        list.forEach(l -> {
            BlockInstruction bi = blockList.stream().filter(b -> b.getType().equals(l)).findAny().get();
            switch (bi.getCollideType()){
                case DETECT_COLLIDE: collide.add(l);
                    break;
                case MATERIAL:
                    material.add(l);
                    break;
                case IGNORE:
                    ignore.add(l);
                    break;
            }
        });
        CommandViewer viewer = (CommandViewer)source;
        viewer.sendMessage(CorePlugin.buildText(TextColours.RED + "|----{Blocks}----|"));
        viewer.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Material: " + TextColours.AQUA + material.size()));
        viewer.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Ignore: " + TextColours.AQUA + ignore.size()));
        viewer.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Detect Collide: " + TextColours.AQUA + collide.size()));
        return true;
    }

    @Override
    public List<String> tab(CommandSource source, String... args) {
        List<String> list = new ArrayList<>();
        if(args.length == 2 && args[1].equalsIgnoreCase("")) {
            list.add("view");
            return list;
        } else if(args.length == 2){
            if("view".startsWith(args[1].toLowerCase())){
                list.add("view");
            }
            return list;
        } else {
            String compare = args[args.length - 1];
            CorePlugin.getPlatform().getBlockTypes().stream().filter(b -> {
                if (b.getId().startsWith(compare)) {
                    return true;
                }
                return b.getId().split(":", 2)[1].startsWith(compare);
            }).forEach(b -> list.add(b.getId()));
            if (list.isEmpty()) {
                CorePlugin.getPlatform().getBlockTypes().forEach(b -> list.add(b.getId()));
            }
        }
        return list;
    }
}
