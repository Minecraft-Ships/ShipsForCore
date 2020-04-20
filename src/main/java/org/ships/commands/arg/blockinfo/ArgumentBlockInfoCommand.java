package org.ships.commands.arg.blockinfo;

import org.core.CorePlugin;
import org.core.command.argument.CommandArgumentLauncher;
import org.core.command.argument.CommandContext;
import org.core.command.argument.arg.ParserArgument;
import org.core.command.argument.arg.generic.AnyAmountArgument;
import org.core.configuration.parser.StringParser;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.world.WorldExtent;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.ships.movement.BlockPriority;
import org.ships.movement.SetMovingBlock;

import java.util.Map;
import java.util.Optional;

public class ArgumentBlockInfoCommand extends CommandArgumentLauncher {

    public ArgumentBlockInfoCommand() {
        super("blockinfo", "Get information about a block", new CommandContext(new ParserArgument<>("BlockType", StringParser.STRING_TO_BLOCK_TYPE)));
    }

    @Override
    public boolean run(CommandContext.CommandArgumentContext context) {
        if(!(context.getSource() instanceof CommandViewer)){
            return false;
        }
        CommandViewer viewer = (CommandViewer) context.getSource();
        Optional<BlockType> opBt = context.getValue("BlockType");
        if(!opBt.isPresent()){
            context.getEntries().stream().forEach(e -> System.out.println("Arg: " + e.getArgument().getId() + " | " + e.getValue().isPresent() + " | " + e.getArgument().getClass().getSimpleName()));
            return false;
        }
        BlockType bt = opBt.get();
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
        return true;
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        return true;
    }
}
