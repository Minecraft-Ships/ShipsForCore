package org.ships.commands.argument.blockinfo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.TranslateCore;
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
import org.core.source.command.CommandSource;
import org.core.utils.Identifiable;
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
import java.util.stream.Collectors;

public class ShipsBlockInfoArgumentCommand implements ArgumentCommand {

    private static final ExactArgument BLOCK_INFO_ARGUMENT = new ExactArgument("blockinfo");
    private static final OptionalArgument<BlockType> BLOCK_TYPE = new OptionalArgument<>(
            new BlockTypeArgument("blocktype"), new ParseCommandArgument<>() {
        @Override
        public CommandArgumentResult<BlockType> parse(CommandContext context,
                                                      CommandArgumentContext<BlockType> argument) {
            if (!(context.getSource() instanceof LivePlayer)) {
                return CommandArgumentResult.from(argument, 0, null);
            }
            Optional<BlockPosition> opBlockType = ((LivePlayer) context.getSource()).getBlockLookingAt();
            return CommandArgumentResult.from(argument, 0, opBlockType.map(Position::getBlockType).orElse(null));
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
        CommandSource viewer = commandContext.getSource();
        BlockType bt = commandContext.getArgument(this, BLOCK_TYPE);
        if (bt == null) {
            viewer.sendMessage(Component.text("BlockType id isn't valid").color(NamedTextColor.RED));
            return false;
        }
        viewer.sendMessage(Component.text("--[" + bt.getName() + "]--"));
        BlockDetails details = bt.getDefaultBlockDetails();
        viewer.sendMessage(Component.text("---[ID]---"));
        viewer.sendMessage(Component.text(" |- ID: " + details.getType().getId()));
        viewer.sendMessage(Component.text(" |- BlockList-CollideType: " + ShipsPlugin
                .getPlugin()
                .getBlockList()
                .getBlockInstruction(details.getType())
                .getCollide()
                .name()));
        viewer.sendMessage(Component.text("---[Keyed Data]---"));
        for (Map.Entry<String, Class<? extends KeyedData<?>>> dataClass : KeyedData.getDefaultKeys().entrySet()) {
            if (details.getUnspecified(dataClass.getValue()).isPresent()) {
                viewer.sendMessage(Component.text(" |- " + dataClass.getKey()));
            }
        }
        if (details.getDirectionalData().isPresent()) {
            viewer.sendMessage(Component.text(" |- Directional"));
        }
        viewer.sendMessage(Component.text("---[Priority]---"));
        WorldExtent world = TranslateCore.getServer().getWorldExtents().findFirst().orElseThrow(() -> new RuntimeException("Minecraft server should always have a world loaded"));
        BlockPriority priority = new SetMovingBlock(world.getPosition(0, 0, 0), world.getPosition(0, 0, 0),
                                                    details).getBlockPriority();
        viewer.sendMessage(Component.text(" |- ID: " + priority.getId()));
        viewer.sendMessage(Component.text(" |- Value: " + priority.getPriorityNumber()));
        viewer.sendMessage(Component.text("---[Like]---"));
        String like = bt
                .getAlike()
                .limit(5)
                .map(Identifiable::getName)
                .collect(Collectors.joining("\n |- "));
        viewer.sendMessage(Component.text("\n |- " + like));
        return true;
    }
}
