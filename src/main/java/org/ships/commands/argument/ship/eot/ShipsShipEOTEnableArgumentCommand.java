package org.ships.commands.argument.ship.eot;

import net.kyori.adventure.text.Component;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.simple.BooleanArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.movement.autopilot.scheduler.EOTExecutor;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.EOTSign;
import org.ships.vessel.sign.ShipsSigns;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ShipsShipEOTEnableArgumentCommand implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_EOT_ARGUMENT = "eot";
    private final String SHIP_ENABLE_ARGUMENT = "enable";
    private final String SHIP_BOOLEAN_ARGUMENT = "boolean";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(this.SHIP_ARGUMENT), new ShipIdArgument<>(this.SHIP_ID_ARGUMENT),
                             new ExactArgument(this.SHIP_EOT_ARGUMENT), new ExactArgument(this.SHIP_ENABLE_ARGUMENT),
                             new BooleanArgument(this.SHIP_BOOLEAN_ARGUMENT));
    }

    @Override
    public String getDescription() {
        return "Enable/Disable the eot of a ship by command";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIP_EOT);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource source = commandContext.getSource();
        Vessel vessel = commandContext.getArgument(this, this.SHIP_ID_ARGUMENT);
        boolean enabled = commandContext.getArgument(this, this.SHIP_BOOLEAN_ARGUMENT);
        EOTSign signFunctions = ShipsSigns.EOT;
        if (!enabled) {
            signFunctions.getScheduler(vessel).forEach(s -> {
                EOTExecutor exe = (EOTExecutor) s.getRunner();
                var sign = exe.getSign();
                sign.ifPresent(liveSignTileEntity -> {
                    var side = signFunctions.getSide(liveSignTileEntity).orElseThrow();
                    side.setLineAt(1, Component.text("Ahead"));
                    side.setLineAt(2, Component.text("{Stop}"));
                });
                s.cancel();
            });
            return true;
        }
        Collection<SyncBlockPosition> eotSigns = vessel.getStructure().getAll(signFunctions);
        if (eotSigns.size() == 1) {
            if (!(source instanceof LivePlayer)) {
                (source).sendMessage(Component.text("Can only enable eot as a player"));

                return false;
            }
            LivePlayer player = (LivePlayer) source;
            LiveTileEntity lste = eotSigns.stream().findAny().get().getTileEntity().get();
            signFunctions.onSecondClick(player, lste.getPosition());
            return true;
        }
        Component text = Component.text("Found more then one EOT sign, unable to enable.");
        source.sendMessage(text);
        return false;
    }
}
