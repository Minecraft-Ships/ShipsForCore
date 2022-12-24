package org.ships.commands.argument.ship.autopilot;

import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.User;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.flag.FlightPathFlag;
import org.ships.vessel.common.types.Vessel;

import java.util.List;
import java.util.Optional;

public class AutopilotCancelArgumentCommand implements ArgumentCommand {

    public static final ExactArgument SHIP_ARGUMENT = new ExactArgument("ship");
    public static final ShipIdArgument<Vessel> SHIP_ID_ARGUMENT = new ShipIdArgument<>("ship_id",
                                                                                       (commandSource, vessel) -> {
                                                                                           Optional<FlightPathFlag> opFlag = vessel.get(
                                                                                                   FlightPathFlag.class);
                                                                                           if (opFlag.isEmpty()) {
                                                                                               return false;
                                                                                           }
                                                                                           if (commandSource instanceof LivePlayer player
                                                                                                   && Permissions.CMD_SHIP_AUTOPILOT_USE_OWN.hasPermission(
                                                                                                   player)) {
                                                                                               return true;
                                                                                           }
                                                                                           if (commandSource instanceof User user
                                                                                                   && vessel instanceof CrewStoredVessel crewVessel) {
                                                                                               return crewVessel
                                                                                                       .getPermission(
                                                                                                               user.getUniqueId())
                                                                                                       .canCommand();
                                                                                           }
                                                                                           return false;
                                                                                       },
                                                                                       vessel -> "Is not running a autopilot");

    @Override
    public List<CommandArgument<?>> getArguments() {
        return List.of(SHIP_ARGUMENT, SHIP_ID_ARGUMENT);
    }

    @Override
    public String getDescription() {
        return "Cancel's an autopilot movement";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIP_AUTOPILOT_USE_OWN);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        Vessel vessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);
        vessel.set(new FlightPathFlag());
        return true;
    }
}
