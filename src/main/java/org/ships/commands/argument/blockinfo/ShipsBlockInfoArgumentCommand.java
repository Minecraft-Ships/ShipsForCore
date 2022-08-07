package org.ships.commands.argument.blockinfo;

import org.array.utils.ArrayUtils;
import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentResult;
import org.core.command.argument.ParseCommandArgument;
import org.core.command.argument.arguments.id.BlockTypeArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.viewer.CommandViewer;
import org.core.utils.Identifiable;
import org.core.world.WorldExtent;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.impl.BlockPosition;
import org.ships.movement.BlockPriority;
import org.ships.movement.SetMovingBlock;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShipsBlockInfoArgumentCommand implements ArgumentCommand {

    private static final ExactArgument BLOCK_INFO_ARGUMENT = new ExactArgument("blockinfo");
    private static final OptionalArgument<BlockType> BLOCK_TYPE = new OptionalArgument<>(
            new BlockTypeArgument("blocktype"), new ParseCommandArgument<BlockType>() {
        @Override
        public CommandArgumentResult<BlockType> parse(CommandContext context,
                CommandArgumentContext<BlockType> argument) {
            if (!(context.getSource() instanceof LivePlayer)) {
                return null;
            }
            LivePlayer player = (LivePlayer) context.getSource();
            Optional<BlockPosition> opBlockType = player.getBlockLookingAt();
            return opBlockType.map(pos -> new CommandArgumentResult<>(0, pos.getBlockType())).orElse(null);
        }
    });

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(BLOCK_INFO_ARGUMENT, BLOCK_TYPE);
    }

    @Override
    public String getDescription() {
        return "Gets the information about a block";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_BLOCK_INFO);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        if (!(commandContext.getSource() instanceof CommandViewer)) {
            return false;
        }
        CommandViewer viewer = (CommandViewer) commandContext.getSource();
        BlockType bt = commandContext.getArgument(this, BLOCK_TYPE);
        if (bt == null) {
            viewer.sendMessage(AText.ofPlain("BlockType id isn't valid").withColour(NamedTextColours.RED));
            return false;
        }
        viewer.sendMessage(AText.ofPlain("--[" + bt.getName() + "]--"));
        BlockDetails details = bt.getDefaultBlockDetails();
        viewer.sendMessage(AText.ofPlain("---[ID]---"));
        viewer.sendMessage(AText.ofPlain(" |- ID: " + details.getType().getId()));
        viewer.sendMessage(AText.ofPlain(" |- BlockList-CollideType: " +
                ShipsPlugin.getPlugin().getBlockList().getBlockInstruction(details.getType()).getCollideType().name()));
        viewer.sendMessage(AText.ofPlain("---[Keyed Data]---"));
        for (Map.Entry<String, Class<? extends KeyedData<?>>> dataClass : KeyedData.getDefaultKeys().entrySet()) {
            if (details.getUnspecified(dataClass.getValue()).isPresent()) {
                viewer.sendMessage(AText.ofPlain(" |- " + dataClass.getKey()));
            }
        }
        if (details.getDirectionalData().isPresent()) {
            viewer.sendMessage(AText.ofPlain(" |- Directional"));
        }
        viewer.sendMessage(AText.ofPlain("---[Priority]---"));
        WorldExtent world = TranslateCore.getServer().getWorlds().iterator().next();
        BlockPriority priority = new SetMovingBlock(world.getPosition(0, 0, 0), world.getPosition(0, 0, 0),
                details).getBlockPriority();
        viewer.sendMessage(AText.ofPlain(" |- ID: " + priority.getId()));
        viewer.sendMessage(AText.ofPlain(" |- Value: " + priority.getPriorityNumber()));
        viewer.sendMessage(AText.ofPlain("---[Like]---"));
        String like = ArrayUtils.toString("\n |- ", Identifiable::getName,
                bt.getLike().parallelStream().limit(5).collect(Collectors.toList()));
        viewer.sendMessage(AText.ofPlain("\n |- " + like));
        return true;
    }
}
