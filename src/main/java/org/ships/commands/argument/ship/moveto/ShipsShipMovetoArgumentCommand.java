package org.ships.commands.argument.ship.moveto;

import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.simple.number.IntegerArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.exceptions.NotEnoughArguments;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.impl.sync.SyncPosition;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.commands.argument.type.ShipIdArgument;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.Arrays;
import java.util.List;

public class ShipsShipMovetoArgumentCommand implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_MOVETO_ARGUMENT = "moveto";
    private final String SHIP_POSX_ARGUMENT = "x";
    private final String SHIP_POSY_ARGUMENT = "y";
    private final String SHIP_POSZ_ARGUMENT = "z";
    private final String SHIP_FACING_ARGUMENT = "facing";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(
                new ExactArgument(SHIP_ARGUMENT),
                new ShipIdArgument<>(SHIP_ID_ARGUMENT),
                new ExactArgument(SHIP_MOVETO_ARGUMENT),
                new IntegerArgument(SHIP_POSX_ARGUMENT),
                new IntegerArgument(SHIP_POSY_ARGUMENT),
                new IntegerArgument(SHIP_POSZ_ARGUMENT)
//                new EnumArgument<>(SHIP_FACING_ARGUMENT)
        );
    }

    @Override
    public String getDescription() {
        return "Move ship to a specific position";
    }

    @Override
    public String getPermissionNode() {
        return Permissions.CMD_SHIP_MOVETO.getPermissionValue();
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        Vessel vessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);
        CommandSource source = commandContext.getSource();
        ((CommandViewer)source).sendMessagePlain("Moving Ship \"" + vessel.getName() + "\"...");

        BasicMovement movement = ShipsPlugin.getPlugin().getConfig().getDefaultMovement();
        MovementContext movementContext = new MovementContext();

        SyncPosition position = vessel.getPosition();
        int x = (int)commandContext.getArgument(this, SHIP_POSX_ARGUMENT) - position.getX().intValue();
        int y = (int)commandContext.getArgument(this, SHIP_POSY_ARGUMENT) - position.getY().intValue();
        int z = (int)commandContext.getArgument(this, SHIP_POSZ_ARGUMENT) - position.getZ().intValue();

        movementContext.setStrictMovement(true);
        movementContext.setMovement(movement);
        vessel.moveTowards(x, y, z, movementContext, exc -> {
            ((CommandViewer)source).sendMessagePlain(exc.toString());
            movementContext.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            if(exc instanceof MoveException){
                MoveException e = (MoveException)exc;
                ((CommandViewer)source).sendMessagePlain("An exception during moving occured.");
            }else{
                movementContext.getEntities().keySet().forEach(s -> {
                    if (s instanceof EntitySnapshot.NoneDestructibleSnapshot){
                        ((EntitySnapshot.NoneDestructibleSnapshot<? extends LiveEntity>) s).getEntity().setGravity(true);
                    }
                });
                exc.printStackTrace();
            }});
        return true;
    }
}
