package org.ships.commands.argument.ship.info;

import org.array.utils.ArrayUtils;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.exceptions.NotEnoughArguments;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.utils.Identifable;
import org.ships.commands.argument.type.ShipIdArgument;
import org.ships.config.configuration.ShipsConfig;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.ShipsVessel;

import java.util.Arrays;
import java.util.List;

public class ShipsShipInfoArgumentCommand implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_INFO_ARGUMENT = "info";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_ARGUMENT), new ShipIdArgument(SHIP_ID_ARGUMENT), new ExactArgument(SHIP_INFO_ARGUMENT));
    }

    @Override
    public String getDescription() {
        return "Information about the specified ship";
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        if(source instanceof CommandViewer){
            return true;
        }
        return false;
    }

    @Override
    public String getPermissionNode() {
        return "";
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        if(!(commandContext.getSource() instanceof CommandViewer)){
            return false;
        }
        CommandViewer viewer = (CommandViewer) commandContext.getSource();
        Vessel vessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);
        viewer.sendMessagePlain("Name: " + vessel.getName());
        if(vessel instanceof Identifable) {
            viewer.sendMessagePlain("ID: " + ((Identifable)vessel).getId());
        }
        viewer.sendMessagePlain("Max Speed: " + vessel.getMaxSpeed());
        viewer.sendMessagePlain("Altitude Speed: " + vessel.getAltitudeSpeed());
        viewer.sendMessagePlain("Size: " + vessel.getStructure().getPositions().size());
        if(vessel instanceof CrewStoredVessel) {
            viewer.sendMessagePlain("Default Permission: " + ((CrewStoredVessel) vessel).getDefaultPermission().getId());
        }
        if(vessel instanceof ShipsVessel) {
            ((ShipsVessel) vessel).getExtraInformation().forEach((key, value) -> viewer.sendMessagePlain(key + ": " + value));
        }
        if(vessel instanceof ShipsVessel) {
            viewer.sendMessagePlain("Flags:");
            viewer.sendMessagePlain(" - " + ArrayUtils.toString("\n - ", f -> {
                if (f instanceof VesselFlag.Serializable) {
                    return ((VesselFlag.Serializable<?>) f).serialize();
                }
                return "";
            }, ((ShipsVessel)vessel).getFlags()));
        }
        viewer.sendMessagePlain("Entities: ");
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        vessel.getEntitiesOvertime(config.getEntityTrackingLimit(), e -> true, e -> {
            String entity = null;
            if (e instanceof LivePlayer) {
                LivePlayer player = (LivePlayer) e;
                entity = "player: " + player.getName();
            } else {
                entity = e.getType().getName();
            }
            viewer.sendMessagePlain("- " + entity);
        }, e -> {});
        return true;
    }
}
