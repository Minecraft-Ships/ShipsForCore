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
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.ShipsSign;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        Set<SyncBlockPosition> set = vessel
                .getStructure()
                .getPositions()
                .stream()
                .filter(p -> ShipsSign.LOCKED_SIGNS.stream().anyMatch(p1 -> p1.equals(p)))
                .collect(Collectors.toSet());
        if (set.isEmpty()) {
            if (source instanceof CommandViewer) {
                ((CommandViewer) source).sendMessage(AText.ofPlain("Cleared all locked signs"));
            }
            ShipsSign.LOCKED_SIGNS.clear();
            return true;
        }
        if (source instanceof CommandViewer) {
            ((CommandViewer) source).sendMessage(AText.ofPlain("Cleared all (" + set.size() + ") locked signs"));
        }
        set.forEach(ShipsSign.LOCKED_SIGNS::remove);
        return true;
    }
}
