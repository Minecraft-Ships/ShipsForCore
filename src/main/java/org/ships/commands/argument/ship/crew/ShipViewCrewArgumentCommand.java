package org.ships.commands.argument.ship.crew;

import org.core.CorePlugin;
import org.core.adventureText.AText;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.arguments.operation.RemainingArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.viewer.CommandViewer;
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
        return Arrays.asList(
                new ExactArgument(SHIP_ARGUMENT),
                new ShipIdArgument<>(SHIP_ID_ARGUMENT, (source, vessel) -> {
                    if (source instanceof LivePlayer && vessel instanceof CrewStoredVessel) {
                        CrewStoredVessel crewVessel = (CrewStoredVessel) vessel;
                        LivePlayer player = (LivePlayer) source;
                        return crewVessel.getPermission(player.getUniqueId()).canCommand();
                    }
                    return vessel instanceof CrewStoredVessel;
                }, v -> "Vessel does not accept crew"),
                new ExactArgument(SHIP_CREW_ARGUMENT),
                new ExactArgument(SHIP_VIEW_ARGUMENT),
                new OptionalArgument<>(
                        new RemainingArgument<>
                                (SHIP_CREW_PERMISSION_ARGUMENT,
                                        new ShipIdentifiableArgument<>(
                                                SHIP_CREW_PERMISSION_ARGUMENT,
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
        if (!(commandContext.getSource() instanceof CommandViewer)) {
            return false;
        }
        CommandViewer viewer = (CommandViewer) commandContext.getSource();
        Set<CrewPermission> permissionsToShow = new HashSet<>(commandContext.getArgument(this, SHIP_CREW_PERMISSION_ARGUMENT));

        CrewStoredVessel vessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);

        if (permissionsToShow.isEmpty()) {
            permissionsToShow.addAll(vessel.getCrew().values());
        }

        permissionsToShow.forEach(crewPermission -> {
            viewer.sendMessage(AText.ofPlain(crewPermission.getName()));
            vessel
                    .getCrew(crewPermission)
                    .stream()
                    .map(uuid -> CorePlugin
                            .getServer()
                            .getOfflineUser(uuid))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(user -> viewer.sendMessage(AText.ofPlain("- " + user.getName())));
        });
        return true;
    }
}
