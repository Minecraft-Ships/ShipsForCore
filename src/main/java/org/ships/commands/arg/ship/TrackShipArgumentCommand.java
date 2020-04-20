package org.ships.commands.arg.ship;

import org.core.CorePlugin;
import org.core.command.argument.CommandArgumentLauncher;
import org.core.command.argument.CommandContext;
import org.core.command.argument.arg.ParserArgument;
import org.core.command.argument.arg.generic.WrappedSuggestionsArgument;
import org.core.command.argument.arg.generic.optional.DefaultArgument;
import org.core.configuration.parser.StringParser;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.core.world.position.block.BlockTypes;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class TrackShipArgumentCommand extends CommandArgumentLauncher {

    public TrackShipArgumentCommand() {
        super("track", "Show the structure of your ship", new CommandContext(new DefaultArgument<>(new WrappedSuggestionsArgument.Fixed(new ParserArgument<>("time", StringParser.STRING_TO_INTEGER), "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), 10)));
    }

    @Override
    public boolean run(CommandContext.CommandArgumentContext context) {
        Optional<Vessel> opVessel = context.getValue("ship");
        int time = context.<Integer>getValue("time").get();
        if(!opVessel.isPresent()){
            if (context.getSource() instanceof CommandViewer) {
                ((CommandViewer) context.getSource()).sendMessage(CorePlugin.buildText(TextColours.RED + "Unknown vessel"));
            }
            return false;
        }
        if (!(context.getSource() instanceof LivePlayer)) {
            if (context.getSource() instanceof CommandViewer) {
                ((CommandViewer) context.getSource()).sendMessage(CorePlugin.buildText(TextColours.RED + "Player only command"));
            }
            return true;
        }
        Vessel vessel = opVessel.get();
        LivePlayer player = (LivePlayer) context.getSource();
        vessel.getStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.OBSIDIAN.get().getDefaultBlockDetails(), player));
        CorePlugin.createSchedulerBuilder()
                .setDisplayName("ShipsTrack:" + vessel.getName())
                .setDelay(time)
                .setDelayUnit(TimeUnit.SECONDS)
                .setExecutor(() -> vessel
                        .getStructure()
                        .getPositions()
                        .forEach(bp -> bp.resetBlock(player)))
                .build(ShipsPlugin.getPlugin())
                .run();
        return false;
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        if(source instanceof LivePlayer){
            if (!((LivePlayer)source).hasPermission(Permissions.CMD_SHIP_TRACK)) {
                return false;
            }
        }
        return true;
    }
}
