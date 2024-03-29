package org.ships.commands.argument.ship.crew;

import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.RemainingArgument;
import org.core.command.argument.arguments.source.UserArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.User;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.permissions.Permissions;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.vessel.common.assits.CrewStoredVessel;

import java.util.*;

public class ShipRemoveCrewArgumentCommand implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_CREW_ARGUMENT = "crew";
    private final String SHIP_VIEW_ARGUMENT = "remove";
    private final String SHIP_PLAYERS_ARGUMENT = "players";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(this.SHIP_ARGUMENT),
                             new ShipIdArgument<>(this.SHIP_ID_ARGUMENT, (source, vessel) -> {
                                 if (source instanceof LivePlayer && vessel instanceof CrewStoredVessel) {
                                     User player = (User) source;
                                     return ((CrewStoredVessel)vessel).getPermission(player.getUniqueId()).canCommand();
                                 }
                                 return vessel instanceof CrewStoredVessel;
                             }, v -> "Vessel does not accept crew"), new ExactArgument(this.SHIP_CREW_ARGUMENT),
                             new ExactArgument(this.SHIP_VIEW_ARGUMENT),
                             new RemainingArgument<>(this.SHIP_PLAYERS_ARGUMENT,
                                                     new UserArgument(this.SHIP_PLAYERS_ARGUMENT)));
    }

    @Override
    public String getDescription() {
        return "Removes a crew member to your ship";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIP_CREW);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CrewStoredVessel vessel = commandContext.getArgument(this, this.SHIP_ID_ARGUMENT);
        Map<UUID, CrewPermission> map = vessel.getCrew();
        List<User> users = commandContext.getArgument(this, this.SHIP_PLAYERS_ARGUMENT);
        users.forEach(user -> map.remove(user.getUniqueId()));
        if (commandContext.getSource() instanceof CommandViewer) {
            ((CommandViewer) commandContext.getSource()).sendMessage(
                    AText.ofPlain("Removed crew member(s)").withColour(NamedTextColours.AQUA));
        }
        return true;
    }
}
