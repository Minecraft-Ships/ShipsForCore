package org.ships.commands.legacy.info;

import org.core.CorePlugin;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.Text;
import org.core.text.TextColours;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.config.blocks.BlockInstruction;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class LegacyInfoCommand implements LegacyArgumentCommand {
    @Override
    public String getName() {
        return "info";
    }

    @Override
    public Optional<String> getPermission() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandSource source, String... args) {
        if(!(source instanceof CommandViewer)){
            return true;
        }
        CommandViewer viewer = (CommandViewer)source;
        viewer.sendMessage(CorePlugin.buildText(TextColours.YELLOW + "----[Ships]----"));
        viewer.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Version: " + TextColours.AQUA + ShipsPlugin.getPlugin().getPluginVersion()));
        viewer.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Vessel Types: " + TextColours.AQUA + ShipsPlugin.getPlugin().getAll(ShipType.class).size()));
        if(contains("shipstype", args) || contains("stype", args)){
            viewer.sendMessage(CorePlugin.buildText(TextColours.AQUA + CorePlugin.toString(TextColours.GREEN + " | " + TextColours.AQUA, st -> st.getDisplayName(), ShipsPlugin.getPlugin().getAll(ShipType.class))));
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
    public List<String> tab(CommandSource source, String... args) {
        List<String> args2 = new ArrayList<>();
        String compare = args[args.length - 1];
        if(("shipstype".startsWith(compare) || "stype".startsWith(compare)) && (contains("shipstype", args) || contains("stype", args))){
            args2.add("shipstype");
        }
        return args2;
    }

    private boolean contains(String arg1, String... args){
        return Stream.of(args).anyMatch(a -> a.equalsIgnoreCase(arg1));
    }
}
