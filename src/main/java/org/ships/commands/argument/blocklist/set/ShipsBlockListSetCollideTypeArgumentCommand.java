package org.ships.commands.argument.blocklist.set;

import org.core.CorePlugin;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.id.BlockTypesArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.simple.EnumArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.core.world.position.block.BlockType;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.DefaultBlockList;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShipsBlockListSetCollideTypeArgumentCommand implements ArgumentCommand {

    private static final String SHIP_BLOCK_LIST_ARGUMENT = "blocklist";
    private static final String SHIP_SET_ARGUMENT = "set";
    private static final String SHIP_COLLIDE_TYPE_ARGUMENT = "collidetype";
    private static final String SHIP_COLLIDE_VALUE_ARGUMENT = "collide_value";
    private static final String SHIP_BLOCK_TYPE_ARGUMENT = "blocktype";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_BLOCK_LIST_ARGUMENT), new ExactArgument(SHIP_SET_ARGUMENT), new ExactArgument(SHIP_COLLIDE_TYPE_ARGUMENT), new EnumArgument<>(SHIP_COLLIDE_VALUE_ARGUMENT, BlockInstruction.CollideType.class), new BlockTypesArgument(SHIP_BLOCK_TYPE_ARGUMENT));
    }

    @Override
    public String getDescription() {
        return "Set the blocklist";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_BLOCKLIST_SET);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        List<BlockType> blocks = commandContext.getArgument(this, SHIP_BLOCK_TYPE_ARGUMENT);
        BlockInstruction.CollideType collideType = commandContext.getArgument(this, SHIP_COLLIDE_VALUE_ARGUMENT);
        DefaultBlockList blocklist = ShipsPlugin.getPlugin().getBlockList();
        blocklist.getBlockList().stream().filter(bi -> blocks.stream().anyMatch(b -> bi.getType().equals(b))).forEach(bi -> {
            blocklist.replaceBlockInstruction(bi.setCollideType(collideType));
        });
        blocklist.saveChanges();
        if(commandContext.getSource() instanceof CommandViewer){
            ((CommandViewer)commandContext.getSource()).sendMessage(CorePlugin.buildText(TextColours.AQUA + "" + blocks.size() + " have been set to " + collideType.name()));
        }
        return true;
    }
}
