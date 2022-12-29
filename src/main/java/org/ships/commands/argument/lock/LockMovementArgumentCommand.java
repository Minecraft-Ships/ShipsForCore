package org.ships.commands.argument.lock;

import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.simple.BooleanArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;

import java.util.List;
import java.util.Optional;

public class LockMovementArgumentCommand implements ArgumentCommand {

    private static final ExactArgument LOCK = new ExactArgument("lock");
    private static final BooleanArgument LOCK_STATUS = new BooleanArgument("status", "lock", "unlock");

    @Override
    public List<CommandArgument<?>> getArguments() {
        return List.of(LOCK, LOCK_STATUS);
    }

    @Override
    public String getDescription() {
        return "Locks all ships from moving";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        return false;
    }
}
