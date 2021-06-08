package org.ships.commands.argument.ship.track;

import org.core.CorePlugin;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.schedule.unit.TimeUnit;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.core.utils.Else;
import org.core.world.position.block.BlockTypes;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.exceptions.NoLicencePresent;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShipsShipTrackArgumentCommand implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String TRACK_ARGUMENT = "track";
    private final String SHIP_ID_ARGUMENT = "ship_id";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_ARGUMENT), new ShipIdArgument<>(SHIP_ID_ARGUMENT), new ExactArgument(TRACK_ARGUMENT));
    }

    @Override
    public String getDescription() {
        return "Tracks the structure of the ship";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIP_TRACK);
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        if (source instanceof LivePlayer) {
            return ((LivePlayer) source).hasPermission(this.getPermissionNode().get());
        }
        return false;
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        Vessel vessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);
        CommandSource source = commandContext.getSource();
        if (!(source instanceof LivePlayer)) {
            if (source instanceof CommandViewer) {
                ((CommandViewer) source).sendMessage(CorePlugin.buildText(TextColours.RED + "Player only command"));
            }
            return true;
        }
        LivePlayer player = (LivePlayer) source;
        vessel.getStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.OBSIDIAN.getDefaultBlockDetails(), (LivePlayer) source));
        CorePlugin.createSchedulerBuilder()
                .setDisplayName("ShipsTrack:" + Else.throwOr(NoLicencePresent.class, vessel::getName, "Unknown"))
                .setDelay(10)
                .setDelayUnit(TimeUnit.SECONDS)
                .setExecutor(() -> vessel
                        .getStructure()
                        .getPositions()
                        .forEach(bp -> bp.resetBlock(player)))
                .build(ShipsPlugin.getPlugin())
                .run();
        return true;
    }
}
