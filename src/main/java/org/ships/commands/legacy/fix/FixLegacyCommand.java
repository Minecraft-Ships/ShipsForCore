package org.ships.commands.legacy.fix;

import org.array.utils.ArrayUtils;
import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.legacy.LegacyArgumentCommand;

import java.util.*;

@Deprecated
public class FixLegacyCommand implements LegacyArgumentCommand {

    @Override
    public String getName() {
        return "fix";
    }

    @Override
    public Optional<String> getPermission() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandSource source, String... args) {
        System.out.println(ArrayUtils.toString(", ", t -> t, args));

        if(args[1].equalsIgnoreCase("noGravity")){
            if(!(source instanceof LivePlayer)){
                return false;
            }
            LivePlayer player = (LivePlayer) source;
            player.setGravity(true);
            return true;
        }
        if(source instanceof CommandViewer){
            ((CommandViewer)source).sendMessage(CorePlugin.buildText("/Ships fix noGravity"));
        }
        return false;
    }

    @Override
    public List<String> tab(CommandSource source, String... args) {
        System.out.println(ArrayUtils.toString(", ", t -> t, args));
        if(args.length == 2 && args[1].equals("")){
            return Collections.singletonList("nogravity");
        }
        if(args.length == 2){
            if(args[1].toLowerCase().startsWith("nogravity")){
                return Collections.singletonList("nogravity");
            }
        }
        return new ArrayList<>();
    }
}
