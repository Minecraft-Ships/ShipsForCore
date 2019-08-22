package org.ships.commands.argument.blockinfo;

import org.core.CorePlugin;
import org.core.command.ChildArgumentCommandLauncher;
import org.core.command.argument.CommandContext;
import org.core.command.argument.arguments.RemainingArguments;
import org.core.command.argument.arguments.block.BlockTypeArgument;
import org.core.command.argument.arguments.generic.SuggestibleParserArgument;
import org.core.configuration.parser.Parser;
import org.core.source.viewer.CommandViewer;
import org.core.world.WorldExtent;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.ships.movement.BlockPriority;
import org.ships.movement.SetMovingBlock;
import org.ships.permissions.Permissions;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class ShipsBlockInfoCommand extends ChildArgumentCommandLauncher {

    public static final String BLOCK_TYPE_ID = "BlockTypeId";

    public ShipsBlockInfoCommand(){
        super(new BlockTypeArgument(BLOCK_TYPE_ID));
    }

    @Override
    protected boolean process(CommandContext context) {
        if(!(context.getSource() instanceof CommandViewer)){
            return false;
        }
        CommandViewer viewer = (CommandViewer) context.getSource();
        Optional<BlockType> opType = context.getArgumentValue(BLOCK_TYPE_ID);
        opType.ifPresent(bt -> {
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
        return true;
    }

    @Override
    public String getName() {
        return "blockinfo";
    }

    @Override
    public String getDescription() {
        return "gains information about a block";
    }

    @Override
    public Optional<String> getPermission() {
        return Optional.of(Permissions.CMD_BLOCK_INFO);
    }
}
