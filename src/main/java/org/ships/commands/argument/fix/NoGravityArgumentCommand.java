package org.ships.commands.argument.fix;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class NoGravityArgumentCommand implements ArgumentCommand {

    private static final String SHIP_FIX_ARGUMENT = "fix";
    private static final String SHIP_NO_GRAVITY = "noGravity";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_FIX_ARGUMENT), new ExactArgument(SHIP_NO_GRAVITY));
    }

    @Override
    public String getDescription() {
        return "Fix no gravity issue";
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        return source instanceof LivePlayer;
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) {
        CommandSource source = commandContext.getSource();
        if (!(source instanceof LivePlayer)) {
            return false;
        }
        LivePlayer player = (LivePlayer) source;
        player.setGravity(true);
        player.sendMessage(
                AText.ofPlain("Other plugins maybe disrupted by this fix").withColour(NamedTextColours.YELLOW));
        return true;
    }
}
