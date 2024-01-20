package org.ships.commands.argument.ship.crew;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.arguments.operation.RemainingArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.User;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.utils.Else;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.vessel.common.assits.CrewStoredVessel;

import java.util.*;

public class ShipViewCrewArgumentCommand implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_CREW_ARGUMENT = "crew";
    private final String SHIP_VIEW_ARGUMENT = "view";
    private final String SHIP_CREW_PERMISSION_ARGUMENT = "permission";

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
                             new ExactArgument(this.SHIP_VIEW_ARGUMENT), new OptionalArgument<>(
                        new RemainingArgument<>(this.SHIP_CREW_PERMISSION_ARGUMENT,
                                                new ShipIdentifiableArgument<>(this.SHIP_CREW_PERMISSION_ARGUMENT,
                                                                               CrewPermission.class)),
                        Collections.emptyList()));
    }

    @Override
    public String getDescription() {
        return "View the crew of the ship";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource viewer = commandContext.getSource();
        Collection<CrewPermission> permissionsToShow = new HashSet<>(
                commandContext.getArgument(this, this.SHIP_CREW_PERMISSION_ARGUMENT));

        CrewStoredVessel vessel = commandContext.getArgument(this, this.SHIP_ID_ARGUMENT);

        if (permissionsToShow.isEmpty()) {
            permissionsToShow.addAll(vessel.getCrew().values());
        }

        permissionsToShow.forEach(crewPermission -> {
            viewer.sendMessage(AText.ofPlain(crewPermission.getName()));
            vessel
                    .getCrew(crewPermission)
                    .stream()
                    .map(uuid -> Else.throwOr(Exception.class,
                                              () -> TranslateCore.getServer().getOfflineUser(uuid).get(),
                                              Optional.<User>empty()))

                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(user -> viewer.sendMessage(AText.ofPlain("- " + user.getName())));
        });
        return true;
    }
}
