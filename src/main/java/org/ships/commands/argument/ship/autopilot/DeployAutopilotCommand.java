package org.ships.commands.argument.ship.autopilot;

import org.core.TranslateCore;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.position.vector.Vector3IntegerArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.schedule.unit.TimeUnit;
import org.core.source.viewer.CommandViewer;
import org.core.utils.Else;
import org.core.vector.type.Vector3;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.movement.autopilot.BasicFlightPath;
import org.ships.movement.autopilot.FlightPath;
import org.ships.movement.autopilot.scheduler.FlightPathExecutor;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.FlightPathType;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DeployAutopilotCommand implements ArgumentCommand {

    private final ExactArgument SHIP_KEY = new ExactArgument("ship");
    private final ShipIdArgument<FlightPathType> SHIP = new ShipIdArgument<>("ship id",
            (s, v) -> v instanceof FlightPathType,
            v -> "Cannot use " + Else.throwOr(IOException.class, v::getName, "") + " with autopilot");
    private final ExactArgument AUTOPILOT = new ExactArgument("autopilot");
    private final ExactArgument ADD = new ExactArgument("add");
    private final Vector3IntegerArgument VECTOR = new Vector3IntegerArgument("vector");

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(
                this.SHIP_KEY,
                this.SHIP,
                this.AUTOPILOT,
                this.ADD,
                this.VECTOR
        );
    }

    @Override
    public String getDescription() {
        return "Move the ship to a specified location without a pilot";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_AUTOPILOT);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        FlightPathType vessel = commandContext.getArgument(this, this.SHIP);
        Vector3<Integer> vectorToAdd = commandContext.getArgument(this, this.VECTOR);
        SyncBlockPosition position = vessel.getPosition().getRelative(vectorToAdd);

        FlightPath bfp = new BasicFlightPath(vessel.getPosition().getPosition(), position.getPosition());
        if (commandContext.getSource() instanceof CommandViewer) {
            bfp.setViewer((CommandViewer) commandContext.getSource());
        }
        vessel.setFlightPath(bfp);
        TranslateCore
                .getScheduleManager().schedule()
                .setIteration(5)
                .setIterationUnit(TimeUnit.SECONDS)
                .setExecutor(new FlightPathExecutor(vessel))
                .setDisplayName("AutoPilot")
                .build(ShipsPlugin.getPlugin()).run();
        return true;
    }
}
