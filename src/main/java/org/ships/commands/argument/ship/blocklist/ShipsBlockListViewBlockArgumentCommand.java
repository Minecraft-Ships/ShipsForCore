package org.ships.commands.argument.ship.blocklist;

import org.core.CorePlugin;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.id.BlockTypeArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.core.world.position.block.BlockType;
import org.ships.config.blocks.BlockInstruction;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;

import java.util.Arrays;
import java.util.List;

public class ShipsBlockListViewBlockArgumentCommand implements ArgumentCommand {

    private static final String SHIP_BLOCK_LIST_ARGUMENT = "blocklist";
    private static final String SHIP_VIEW_ARGUMENT = "view";
    private static final String SHIP_BLOCK_TYPE_ARGUMENT = "blocktype";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_BLOCK_LIST_ARGUMENT), new ExactArgument(SHIP_VIEW_ARGUMENT), new BlockTypeArgument(SHIP_BLOCK_TYPE_ARGUMENT));
    }

    @Override
    public String getDescription() {
        return "View BlockList information about a block";
    }

    @Override
    public String getPermissionNode() {
        return Permissions.CMD_BLOCKLIST_VIEW.getPermissionValue();
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        if(source instanceof LivePlayer){
            return ((LivePlayer) source).hasPermission(this.getPermissionNode());
        }
        return source instanceof CommandViewer;
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        if(!(commandContext.getSource() instanceof CommandViewer)){
            return false;
        }
        CommandViewer viewer = (CommandViewer)commandContext.getSource();
        BlockType type = commandContext.getArgument(this, SHIP_BLOCK_TYPE_ARGUMENT);
        CommandSource source = commandContext.getSource();
        BlockInstruction bi = ShipsPlugin.getPlugin().getBlockList().getBlockInstruction(type);
        viewer.sendMessage(CorePlugin.buildText(TextColours.AQUA + "CollideType: " + TextColours.YELLOW + bi.getCollideType().name()));
        viewer.sendMessage(CorePlugin.buildText(TextColours.AQUA + "BlockLimit: " + TextColours.YELLOW + bi.getBlockLimit()));
        return true;
    }
}
