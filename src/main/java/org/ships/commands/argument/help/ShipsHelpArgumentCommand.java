package org.ships.commands.argument.help;

import org.array.utils.ArrayUtils;
import org.core.CorePlugin;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.ships.commands.argument.ShipsArgumentCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShipsHelpArgumentCommand implements ArgumentCommand {

    private static final String HELP_ARGUMENT = "help";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Collections.singletonList(new OptionalArgument<>(new ExactArgument(HELP_ARGUMENT), HELP_ARGUMENT));
    }

    @Override
    public String getDescription() {
        return "List all commands";
    }

    @Override
    public String getPermissionNode() {
        return "";
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        return true;
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource source = commandContext.getSource();
        if(!(source instanceof CommandViewer)){
            return false;
        }
        CommandViewer viewer = (CommandViewer)source;
        for(ArgumentCommand cmd : ShipsArgumentCommand.COMMANDS){
            if(!cmd.hasPermission(viewer)){
                continue;
            }
            viewer.sendMessage(CorePlugin.buildText(TextColours.AQUA + ArrayUtils.toString(" ", CommandArgument::getUsage, cmd.getArguments()) + ": " + TextColours.YELLOW + cmd.getDescription()));
        }
        return true;
    }
}
