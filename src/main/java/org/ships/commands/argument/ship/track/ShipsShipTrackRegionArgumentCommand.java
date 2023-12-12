package org.ships.commands.argument.ship.track;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.TranslateCore;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.schedule.unit.TimeUnit;
import org.core.source.command.CommandSource;
import org.core.utils.Bounds;
import org.core.world.WorldExtent;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShipsShipTrackRegionArgumentCommand implements ArgumentCommand {

    private final ShipIdArgument<Vessel> idArgument = new ShipIdArgument<>("ship_id");

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument("ship"), this.idArgument, new ExactArgument("track"),
                             new ExactArgument("region"));
    }

    @Override
    public String getDescription() {
        return "Tracks the region of the ship";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIP_TRACK_OWN);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        Vessel vessel = commandContext.getArgument(this, this.idArgument);
        if (!(commandContext.getSource() instanceof LivePlayer player)) {
            commandContext.getSource().sendMessage(Component.text("Player only command").color(NamedTextColor.RED));
            return true;
        }
        Bounds<Integer> bounds = vessel.getStructure().getBounds();
        BlockDetails bedrock = BlockTypes.BEDROCK.getDefaultBlockDetails();
        WorldExtent world = vessel.getPosition().getWorld();
        bounds.frame().forEach(vector -> world.getPosition(vector).setBlock(bedrock, player));
        TranslateCore
                .getScheduleManager()
                .schedule()
                .setDisplayName("reset region")
                .setDelayUnit(TimeUnit.SECONDS)
                .setDelay(10)
                .setRunner(scheduler -> bounds.frame().forEach(vector -> world.getPosition(vector).resetBlock(player)))
                .buildDelayed(ShipsPlugin.getPlugin()).run();
        return true;
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        if (source instanceof LivePlayer) {
            return ((LivePlayer) source).hasPermission(this.getPermissionNode().get());
        }
        return false;
    }
}
