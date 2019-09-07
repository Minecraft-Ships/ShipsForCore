package org.ships.commands.argument.info;

import org.core.CorePlugin;
import org.core.command.ChildArgumentCommandLauncher;
import org.core.command.argument.CommandContext;
import org.core.command.argument.arguments.RemainingArguments;
import org.core.command.argument.arguments.generic.StringArgument;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.Text;
import org.core.text.TextColours;
import org.ships.config.blocks.BlockInstruction;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ShipsInfoCommand extends ChildArgumentCommandLauncher {

    public static final String ARGUMENT_ID = "Argument";

    public ShipsInfoCommand(){
        super(new RemainingArguments<>(new StringArgument.Suggestible(ARGUMENT_ID, "shipstype")));
    }

    @Override
    protected boolean process(CommandContext context) {
        if(!(context.getSource() instanceof CommandViewer)){
            return true;
        }
        CommandViewer viewer = (CommandViewer)context.getSource();
        Optional<List<String>> opArgs = context.getArgumentValue(ARGUMENT_ID);
        viewer.sendMessage(CorePlugin.buildText(TextColours.YELLOW + "----[Ships]----"));
        viewer.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Version: " + TextColours.AQUA + ShipsPlugin.getPlugin().getPluginVersion()));
        viewer.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Vessel Types: " + TextColours.AQUA + ShipsPlugin.getPlugin().getAll(ShipType.class).size()));
        if(opArgs.isPresent()){
            List<String> args = opArgs.get();
            if(args.contains("shipstype") || args.contains("stype")){
                viewer.sendMessage(CorePlugin.buildText(TextColours.AQUA + CorePlugin.toString(TextColours.GREEN + " | " + TextColours.AQUA, st -> st.getDisplayName(), ShipsPlugin.getPlugin().getAll(ShipType.class))));
            }
        }
        Set<BlockInstruction> blockList = ShipsPlugin.getPlugin().getBlockList().getBlockList();
        Text blockListText = null;
        for(BlockInstruction.CollideType collideType : BlockInstruction.CollideType.values()){
            if(blockListText == null){
                blockListText = CorePlugin.buildText(TextColours.GREEN + collideType.name() + ": " + TextColours.AQUA + blockList.stream().filter(b -> b.getCollideType().equals(collideType)).count());
            }else{
                blockListText = blockListText.append(CorePlugin.buildText(TextColours.GREEN + collideType.name() + ": " + TextColours.AQUA + blockList.stream().filter(b -> b.getCollideType().equals(collideType)).count()));

            }
        }
        viewer.sendMessage(blockListText);
        return true;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "gain information about the Ships plugin";
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        if(source instanceof LivePlayer){
            return ((LivePlayer) source).hasPermission(Permissions.CMD_INFO);
        }
        return true;
    }

    @Override
    public String getUsage(CommandSource source){
        return "/ships info [\"shipstype\"]";
    }
}
