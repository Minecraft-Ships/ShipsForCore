package org.ships.commands.argument.blockinfo;

import org.array.utils.ArrayUtils;
import org.core.CorePlugin;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.id.BlockTypeArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.core.utils.Identifable;
import org.core.world.WorldExtent;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.ships.movement.BlockPriority;
import org.ships.movement.SetMovingBlock;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ShipsBlockInfoArgumentCommand implements ArgumentCommand {

    private static final String BLOCK_INFO_ARGUMENT = "blockinfo";
    private static final String BLOCK_TYPE = "blocktype";
    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(BLOCK_INFO_ARGUMENT), new OptionalArgument<>(new BlockTypeArgument(BLOCK_TYPE), new OptionalArgument.Parser<BlockType>() {

            @Override
            public BlockType parse(CommandContext context, CommandArgumentContext<BlockType> argument) {
                if (!(context.getSource() instanceof LivePlayer)) {
                    return null;
                }
                LivePlayer player = (LivePlayer)context.getSource();
                Optional<BlockPosition> opBlockType = player.getBlockLookingAt();
                return opBlockType.map(Position::getBlockType).orElse(null);
            }
        }));
    }

    @Override
    public String getDescription() {
        return "Gets the information about a block";
    }

    @Override
    public String getPermissionNode() {
        return Permissions.CMD_BLOCK_INFO;
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        if(!(commandContext.getSource() instanceof CommandViewer)){
            return false;
        }
        CommandViewer viewer = (CommandViewer) commandContext.getSource();
        BlockType bt = commandContext.getArgument(this, BLOCK_TYPE);
        if(bt == null){
            viewer.sendMessage(CorePlugin.buildText(TextColours.RED + "BlockType id isn't valid"));
            return false;
        }
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
        return true;
    }
}
