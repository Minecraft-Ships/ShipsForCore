package org.ships.commands.argument.ship.autopilot;

import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.position.vector.Vector3IntegerArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.User;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.vector.type.Vector3;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.movement.autopilot.path.FlightPath;
import org.ships.movement.autopilot.path.FlightPathBuilder;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.movement.instruction.details.SimpleMovementException;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.flag.FlightPathFlag;
import org.ships.vessel.common.types.Vessel;

import java.util.List;
import java.util.Optional;

public class AutopilotToArgumentCommand implements ArgumentCommand {

    public static final ExactArgument SHIP_ARGUMENT = new ExactArgument("ship");
    public static final ShipIdArgument<Vessel> SHIP_ID_ARGUMENT = new ShipIdArgument<>("ship_id",
                                                                                       (commandSource, vessel) -> {
                                                                                           if (!(vessel.getType() instanceof AirType)) {
                                                                                               return false;
                                                                                           }
                                                                                           if (commandSource instanceof LivePlayer
                                                                                                   && Permissions.CMD_SHIP_AUTOPILOT_USE_OWN.hasPermission(
                                                                                                   (LivePlayer) commandSource)) {
                                                                                               return true;
                                                                                           }
                                                                                           if (commandSource instanceof User
                                                                                                   && vessel instanceof CrewStoredVessel) {
                                                                                               return ((CrewStoredVessel)vessel)
                                                                                                       .getPermission(
                                                                                                               ((User)commandSource).getUniqueId())
                                                                                                       .canCommand();
                                                                                           }
                                                                                           return false;
                                                                                       },
                                                                                       vessel -> "Cannot be autopiloted");
    public static final ExactArgument TO = new ExactArgument("to");
    public static final Vector3IntegerArgument POSITION_ARGUMENT = new Vector3IntegerArgument("location");

    @Override
    public List<CommandArgument<?>> getArguments() {
        return List.of(SHIP_ARGUMENT, SHIP_ID_ARGUMENT, TO, POSITION_ARGUMENT);
    }

    @Override
    public String getDescription() {
        return "Move automatically to a location";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIP_AUTOPILOT_USE_OWN);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        Vessel vessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);
        Vector3<Integer> position = commandContext.getArgument(this, POSITION_ARGUMENT);
        FlightPath flightPath = new FlightPathBuilder().ofAutopilot(vessel.getPosition(), position, 240).build();

        MovementDetailsBuilder details = new MovementDetailsBuilder().setException(
                new SimpleMovementException(commandContext.getSource()));

        FlightPathFlag flag = new FlightPathFlag();
        flag.setValue(flightPath);
        flag.setMovementDetail(details);
        return true;
    }
}
