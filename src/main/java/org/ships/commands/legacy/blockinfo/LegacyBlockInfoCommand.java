package org.ships.commands.legacy.blockinfo;

import org.array.utils.ArrayUtils;
import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.utils.Identifable;
import org.core.world.WorldExtent;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.movement.BlockPriority;
import org.ships.movement.SetMovingBlock;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;

import java.util.*;

public class LegacyBlockInfoCommand implements LegacyArgumentCommand {
    @Override
    public String getName() {
        return "blockinfo";
    }

    @Override
    public Optional<String> getPermission() {
        return Optional.of(Permissions.CMD_BLOCK_INFO.getPermissionValue());
    }

    @Override
    public boolean run(CommandSource source, String... args) {
        if(!(source instanceof CommandViewer)){
            return false;
        }
        CommandViewer viewer = (CommandViewer) source;
        List<String> ids = new ArrayList<>(Arrays.asList(args));
        ids.remove(0);
        if(ids.isEmpty() && source instanceof LivePlayer){
            ((LivePlayer)source).getBlockLookingAt().ifPresent(p -> ids.add(p.getBlockDetails().getType().getId()));
        }
        ids.forEach(id -> {
            CorePlugin.getPlatform().getBlockType(id).ifPresent(bt -> {
                viewer.sendMessagePlain("--[" + bt.getName() + "]--");
                BlockDetails details = bt.getDefaultBlockDetails();
                viewer.sendMessagePlain("---[ID]---");
                viewer.sendMessagePlain(" |- ID: " + details.getType().getId());
                viewer.sendMessagePlain(" |- BlockList-CollideType: " + ShipsPlugin.getPlugin().getBlockList().getBlockInstruction(details.getType()).getCollideType().name());
                viewer.sendMessagePlain("---[Keyed Data]---");
                for(Map.Entry<String, Class<? extends KeyedData<?>>> dataClass : KeyedData.getDefaultKeys().entrySet()){
                    if (details.getUnspecified(dataClass.getValue()).isPresent()){
                        viewer.sendMessagePlain(" |- " + dataClass.getKey());
                    }
                }
                if(details.getDirectionalData().isPresent()){
                    viewer.sendMessagePlain(" |- Directional");
                }
                viewer.sendMessagePlain("---[Priority]---");
                WorldExtent world = CorePlugin.getServer().getWorlds().iterator().next();
                BlockPriority priority = new SetMovingBlock(world.getPosition(0, 0, 0), world.getPosition(0, 0, 0), details).getBlockPriority();
                viewer.sendMessagePlain(" |- ID: " + priority.getId());
                viewer.sendMessagePlain(" |- Value: " + priority.getPriorityNumber());
                viewer.sendMessagePlain("---[Like]---");
                String like = ArrayUtils.toString("\n |- ", Identifable::getName, bt.getLike());
                viewer.sendMessagePlain("\n |- " + like);
            });
        });
        return true;
    }

    @Override
    public List<String> tab(CommandSource source, String... args) {
        List<String> list = new ArrayList<>();
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
        return list;
    }
}
