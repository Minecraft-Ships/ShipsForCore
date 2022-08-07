package org.ships.commands.argument.ship.track;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
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
        return Arrays.asList(new ExactArgument(this.SHIP_ARGUMENT), new ShipIdArgument<>(this.SHIP_ID_ARGUMENT),
                new ExactArgument(this.TRACK_ARGUMENT));
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
        Vessel vessel = commandContext.getArgument(this, this.SHIP_ID_ARGUMENT);
        CommandSource source = commandContext.getSource();
        if (!(source instanceof LivePlayer player)) {
            if (source instanceof CommandViewer) {
                ((CommandViewer) source).sendMessage(
                        AText.ofPlain("Player only command").withColour(NamedTextColours.RED));
            }
            return true;
        }
        vessel
                .getStructure()
                .getPositions()
                .forEach(bp -> bp.setBlock(BlockTypes.OBSIDIAN.getDefaultBlockDetails(), (LivePlayer) source));
        TranslateCore
                .getScheduleManager()
                .schedule()
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
