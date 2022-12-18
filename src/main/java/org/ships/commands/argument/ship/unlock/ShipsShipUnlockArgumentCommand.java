package org.ships.commands.argument.ship.unlock;

import org.core.adventureText.AText;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.lock.SignLock;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ShipsShipUnlockArgumentCommand implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_UNLOCK_ARGUMENT = "unlock";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(this.SHIP_ARGUMENT), new ShipIdArgument<>(this.SHIP_ID_ARGUMENT),
                             new ExactArgument(this.SHIP_UNLOCK_ARGUMENT));
    }

    @Override
    public String getDescription() {
        return "Unlocks the signs on the ship if error occurs";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource source = commandContext.getSource();
        Vessel vessel = commandContext.getArgument(this, this.SHIP_ID_ARGUMENT);
        Collection<SignLock> locks = ShipsPlugin.getPlugin().getLockedSignManager().getLockedSigns(vessel);
        if (locks.isEmpty()) {
            if (source instanceof CommandViewer) {
                ((CommandViewer) source).sendMessage(AText.ofPlain("No locks could be found"));
            }
            return true;
        }
        boolean successful = ShipsPlugin.getPlugin().getLockedSignManager().unlockAll(locks);
        if (source instanceof CommandViewer) {
            ((CommandViewer) source).sendMessage(AText.ofPlain(
                    (successful ? "Cleared " : "Failed to clear") + " all (" + locks.size() + ") locked signs: "));
        }
        return true;
    }
}
