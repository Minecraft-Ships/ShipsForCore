package org.ships.commands.argument.info;

import org.array.utils.ArrayUtils;
import org.core.CorePlugin;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.Text;
import org.core.text.TextColours;
import org.ships.config.blocks.BlockInstruction;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ShipsInfoArgumentCommand implements ArgumentCommand {

    private static final String INFO_ARGUMENT = "info";
    private static final String SHIP_TYPE_ARGUMENT = "ship_type_flag";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(INFO_ARGUMENT), new OptionalArgument<>(new ExactArgument(SHIP_TYPE_ARGUMENT, false, "shipstype", "stype"), (String) null));
    }

    @Override
    public String getDescription() {
        return "Basic information about the Ships plugin";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_INFO);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource source = commandContext.getSource();
        if (!(source instanceof CommandViewer)) {
            return true;
        }
        CommandViewer viewer = (CommandViewer) source;
        viewer.sendMessage(CorePlugin.buildText(TextColours.YELLOW + "----[Ships]----"));
        viewer.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Version: " + TextColours.AQUA + ShipsPlugin.getPlugin().getPluginVersion()));
        viewer.sendMessage(CorePlugin.buildText(TextColours.GREEN + ShipsPlugin.PRERELEASE_TAG + " Version: " + TextColours.AQUA + ShipsPlugin.PRERELEASE_VERSION));
        viewer.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Vessel Types: " + TextColours.AQUA + ShipsPlugin.getPlugin().getAll(ShipType.class).size()));
        if (commandContext.getArgument(this, SHIP_TYPE_ARGUMENT) != null) {
            viewer.sendMessage(CorePlugin.buildText(TextColours.AQUA + ArrayUtils.toString(TextColours.GREEN + " | " + TextColours.AQUA, ShipType::getDisplayName, ShipsPlugin.getPlugin().getAll(ShipType.class))));
        }
        Set<BlockInstruction> blockList = ShipsPlugin.getPlugin().getBlockList().getBlockList();
        Text blockListText = null;
        for (BlockInstruction.CollideType collideType : BlockInstruction.CollideType.values()) {
            if (blockListText == null) {
                blockListText = CorePlugin.buildText(TextColours.GREEN + collideType.name() + ": " + TextColours.AQUA + blockList.stream().filter(b -> b.getCollideType().equals(collideType)).count());
            } else {
                blockListText = blockListText.append(CorePlugin.buildText(", " + TextColours.GREEN + collideType.name() + ": " + TextColours.AQUA + blockList.stream().filter(b -> b.getCollideType().equals(collideType)).count()));

            }
        }
        viewer.sendMessage(blockListText);
        //viewer.sendMessagePlain("Locked Sign: " + ArrayUtils.toString("| ", b -> "[" + b.getX() + "," + b.getY() + "," + b.getZ() + "]" , ShipsSign.LOCKED_SIGNS));
        return true;
    }
}
