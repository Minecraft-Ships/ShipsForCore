package org.ships.commands.argument.help;

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

import java.util.*;

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
        Collection<ArgumentCommand> commands = new TreeSet<>((o1, o2) -> {
            List<CommandArgument<?>> a1 = o1.getArguments();
            List<CommandArgument<?>> a2 = o2.getArguments();
            if (a1.isEmpty() && a2.isEmpty()) {
                return 0;
            }
            if (a1.isEmpty()) {
                return 1;
            }
            if (a2.isEmpty()) {
                return -1;
            }
            CommandArgument<?> arg1 = a1.get(0);
            CommandArgument<?> arg2 = a2.get(0);
            return arg1.getUsage().compareTo(arg2.getUsage());
        });
        for (ArgumentCommand cmd : ShipsArgumentCommand.COMMANDS) {
            if (!cmd.hasPermission(viewer)) {
                continue;
            }
            List<CommandArgument<?>> arguments = cmd.getArguments();
            if (arguments.isEmpty()) {
                commands.add(cmd);
                continue;
            }
            String usage = arguments.get(0).getUsage();
            boolean result = commands.parallelStream().anyMatch(argCmd -> {
                List<CommandArgument<?>> commandArgs = argCmd.getArguments();
                if (commandArgs.isEmpty()) {
                    return false;
                }
                return usage.equals(commandArgs.get(0).getUsage());
            });
            if (result) {
                continue;
            }
            commands.add(cmd);
        }
        for (ArgumentCommand cmd : commands) {
            List<CommandArgument<?>> arguments = cmd.getArguments();
            if (arguments.isEmpty()) {
                viewer.sendMessage(AText.ofPlain(cmd.getDescription()).withColour(NamedTextColours.YELLOW));
                continue;
            }
            viewer.sendMessage(
                    AText.ofPlain(arguments.get(0).getUsage() + ":")
                            .withColour(NamedTextColours.AQUA)
                            .append(
                                    AText
                                            .ofPlain(cmd.getDescription())
                                            .withColour(NamedTextColours.YELLOW)));
        }
        return true;
    }
}
