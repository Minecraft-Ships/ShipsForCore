package org.ships.commands.argument.blocklist;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.id.BlockTypeArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.world.position.block.BlockType;
import org.ships.config.blocks.instruction.BlockInstruction;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShipsBlockListViewBlockArgumentCommand implements ArgumentCommand {

    private static final String SHIP_BLOCK_LIST_ARGUMENT = "blocklist";
    private static final String SHIP_VIEW_ARGUMENT = "view";
    private static final String SHIP_BLOCK_TYPE_ARGUMENT = "blocktype";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_BLOCK_LIST_ARGUMENT), new ExactArgument(SHIP_VIEW_ARGUMENT),
                             new BlockTypeArgument(SHIP_BLOCK_TYPE_ARGUMENT));
    }

    @Override
    public String getDescription() {
        return "View BlockList information about a block";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_BLOCKLIST_VIEW);
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        if (source instanceof LivePlayer) {
            return ((LivePlayer) source).hasPermission(this.getPermissionNode().get());
        }
        return source instanceof CommandViewer;
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        if (!(commandContext.getSource() instanceof CommandViewer)) {
            return false;
        }
        CommandViewer viewer = (CommandViewer) commandContext.getSource();
        BlockType type = commandContext.getArgument(this, SHIP_BLOCK_TYPE_ARGUMENT);
        BlockInstruction bi = ShipsPlugin.getPlugin().getBlockList().getBlockInstruction(type);
        AText collideType = AText
                .ofPlain("CollideType: ")
                .withColour(NamedTextColours.AQUA)
                .append(AText.ofPlain(bi.getCollide().name()).withColour(NamedTextColours.YELLOW));
        AText blockLimit = AText
                .ofPlain("BlockLimit: ")
                .withColour(NamedTextColours.AQUA)
                .append(AText.ofPlain(String.valueOf(bi.getBlockLimit())).withColour(NamedTextColours.YELLOW));
        viewer.sendMessage(collideType);
        viewer.sendMessage(blockLimit);
        return true;
    }
}
