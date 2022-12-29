package org.ships.commands.argument.blocklist;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.config.blocks.instruction.BlockInstruction;
import org.ships.config.blocks.instruction.CollideType;
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
        if (!(commandContext.getSource() instanceof CommandViewer viewer)) {
            return false;
        }
        Collection<BlockInstruction> bl = ShipsPlugin.getPlugin().getBlockList().getBlockList();
        Map<CollideType, Integer> values = new EnumMap<>(CollideType.class);
        for (CollideType type : CollideType.values()) {
            values.put(type, 0);
        }
        for (BlockInstruction bi : bl) {
            values.replace(bi.getCollide(), values.get(bi.getCollide()) + 1);
        }
        values.forEach((c, a) -> viewer.sendMessage(AText
                                                            .ofPlain(c.name() + ": ")
                                                            .withColour(NamedTextColours.AQUA)
                                                            .append(AText
                                                                            .ofPlain(a.toString())
                                                                            .withColour(NamedTextColours.YELLOW))));
        return true;
    }
}
