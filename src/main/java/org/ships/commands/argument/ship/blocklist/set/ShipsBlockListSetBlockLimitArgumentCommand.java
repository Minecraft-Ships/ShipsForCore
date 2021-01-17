package org.ships.commands.argument.ship.blocklist.set;

import org.core.CorePlugin;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.id.BlockTypesArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.simple.EnumArgument;
import org.core.command.argument.arguments.simple.number.IntegerArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.core.world.position.block.BlockType;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.DefaultBlockList;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;

import java.util.Arrays;
import java.util.List;

public class ShipsBlockListSetBlockLimitArgumentCommand implements ArgumentCommand {

    private static final String SHIP_BLOCK_LIST_ARGUMENT = "blocklist";
    private static final String SHIP_SET_ARGUMENT = "set";
    private static final String SHIP_BLOCK_LIMIT_ARGUMENT = "blocklimit";
    private static final String SHIP_LIMIT_VALUE_ARGUMENT = "limit_value";
    private static final String SHIP_BLOCK_TYPE_ARGUMENT = "blocktype";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_BLOCK_LIST_ARGUMENT), new ExactArgument(SHIP_SET_ARGUMENT), new ExactArgument(SHIP_BLOCK_LIMIT_ARGUMENT), new IntegerArgument(SHIP_LIMIT_VALUE_ARGUMENT), new BlockTypesArgument(SHIP_BLOCK_TYPE_ARGUMENT));
    }

    @Override
    public String getDescription() {
        return "Set the blocklist";
    }

    @Override
    public String getPermissionNode() {
        return Permissions.CMD_BLOCKLIST_SET.getPermissionValue();
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        List<BlockType> blocks = commandContext.getArgument(this, SHIP_BLOCK_TYPE_ARGUMENT);
        int limit = commandContext.getArgument(this, SHIP_LIMIT_VALUE_ARGUMENT);
        DefaultBlockList blocklist = ShipsPlugin.getPlugin().getBlockList();
        blocklist.getBlockList().stream().filter(bi -> blocks.stream().anyMatch(b -> bi.getType().equals(b))).forEach(bi -> {
            blocklist.replaceBlockInstruction(bi.setBlockLimit(limit));
        });
        blocklist.saveChanges();
        if(commandContext.getSource() instanceof CommandViewer){
            ((CommandViewer)commandContext.getSource()).sendMessage(CorePlugin.buildText(TextColours.AQUA + "" + blocks.size() + " have been set to have a block limit of " + limit));
        }
        return true;
    }
}
