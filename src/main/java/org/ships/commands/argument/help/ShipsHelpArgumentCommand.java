package org.ships.commands.argument.help;

import org.array.utils.ArrayUtils;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.argument.ShipsArgumentCommand;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    public Optional<Permission> getPermissionNode() {
        return Optional.empty();
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        return true;
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource source = commandContext.getSource();
        if (!(source instanceof CommandViewer)) {
            return false;
        }
        CommandViewer viewer = (CommandViewer) source;
        for (ArgumentCommand cmd : ShipsArgumentCommand.COMMANDS) {
            if (!cmd.hasPermission(viewer)) {
                continue;
            }
            viewer.sendMessage(
                    AText.ofPlain(ArrayUtils.toString(" ", CommandArgument::getUsage, cmd.getArguments()) + ":")
                            .withColour(NamedTextColours.AQUA)
                            .append(
                                    AText
                                            .ofPlain(cmd.getDescription())
                                            .withColour(NamedTextColours.YELLOW)));
        }
        return true;
    }
}
