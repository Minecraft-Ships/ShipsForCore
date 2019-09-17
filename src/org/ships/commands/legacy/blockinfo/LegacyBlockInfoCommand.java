package org.ships.commands.legacy.blockinfo;

import org.core.CorePlugin;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.world.WorldExtent;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.movement.BlockPriority;
import org.ships.movement.SetMovingBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LegacyBlockInfoCommand implements LegacyArgumentCommand {
    @Override
    public String getName() {
        return "blockinfo";
    }

    @Override
    public boolean run(CommandSource source, String... args) {
        if(!(source instanceof CommandViewer)){
            return false;
        }
        CommandViewer viewer = (CommandViewer) source;
        List<String> ids = new ArrayList<>(Arrays.asList(args));
        ids.remove(0);
        ids.forEach(id -> {
            CorePlugin.getPlatform().getBlockType(id).ifPresent(bt -> {
                viewer.sendMessagePlain("--[" + bt.getName() + "]--");
                BlockDetails details = bt.getDefaultBlockDetails();
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
                String like = CorePlugin.toString("\n |- ", f -> f.getName(), bt.getLike());
                viewer.sendMessagePlain("\n |- " + (like == null ? "NONE" : like));
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
