package org.ships.commands.argument.ship.blocklist;

import org.core.CorePlugin;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.ships.config.blocks.BlockInstruction;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;

import java.util.*;

public class ShipsBlockListViewArgumentCommand implements ArgumentCommand {

    private static final String SHIP_BLOCK_LIST_ARGUMENT = "blocklist";
    private static final String SHIP_VIEW_ARGUMENT = "view";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_BLOCK_LIST_ARGUMENT), new ExactArgument(SHIP_VIEW_ARGUMENT));
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
        CommandSource source = commandContext.getSource();
        Set<BlockInstruction> bl = ShipsPlugin.getPlugin().getBlockList().getBlockList();
        Map<BlockInstruction.CollideType, Integer> values = new HashMap<>();
        for(BlockInstruction.CollideType type : BlockInstruction.CollideType.values()){
            values.put(type, 0);
        }
        for(BlockInstruction bi : bl){
            values.replace(bi.getCollideType(), values.get(bi.getCollideType()) + 1);
        }
        values.forEach((c, a) -> viewer.sendMessage(CorePlugin.buildText(TextColours.AQUA + c.name() + ": " + TextColours.YELLOW + a)));
        return true;
    }
}
