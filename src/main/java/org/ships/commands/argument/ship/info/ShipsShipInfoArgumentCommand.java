package org.ships.commands.argument.ship.info;

import org.array.utils.ArrayUtils;
import org.core.adventureText.AText;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.utils.Else;
import org.jetbrains.annotations.NotNull;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.config.messages.Message;
import org.ships.exceptions.NoLicencePresent;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.assits.IdentifiableShip;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.ShipsVessel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ShipsShipInfoArgumentCommand implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_INFO_ARGUMENT = "info";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_ARGUMENT), new ShipIdArgument<>(SHIP_ID_ARGUMENT), new ExactArgument(SHIP_INFO_ARGUMENT));
    }

    @Override
    public String getDescription() {
        return "Information about the specified ship";
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        return source instanceof CommandViewer;
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        if (!(commandContext.getSource() instanceof CommandViewer)) {
            return false;
        }
        AdventureMessageConfig messages = ShipsPlugin.getPlugin().getAdventureMessageConfig();
        CommandViewer viewer = (CommandViewer) commandContext.getSource();
        Vessel vessel = commandContext.getArgument(this, SHIP_ID_ARGUMENT);
        AText infoName = AdventureMessageConfig
                .INFO_NAME
                .parse(messages)
                .withAllAs(
                        "%" + Message.VESSEL_NAME.adapterText() + "%",
                        AText.ofPlain(Else.throwOr(NoLicencePresent.class, vessel::getName, "Unknown")));
        if (vessel instanceof IdentifiableShip) {
            IdentifiableShip ship = (IdentifiableShip) vessel;
            AText infoId = AdventureMessageConfig.INFO_ID.parse(messages).withAllAs("%" + Message.VESSEL_ID.adapterText() + "%", AText.ofPlain(Else.throwOr(NoLicencePresent.class, ship::getId, "Unknown")));
            viewer.sendMessage(infoId);
        }
        AText maxSpeed = AdventureMessageConfig.INFO_MAX_SPEED.parse(messages).withAllAs("%" + Message.VESSEL_SPEED.adapterText() + "%", AText.ofPlain(vessel.getMaxSpeed() + ""));
        AText altitudeSpeed = AdventureMessageConfig.INFO_ALTITUDE_SPEED.parse(messages).withAllAs("%" + Message.VESSEL_SPEED.adapterText() + "%", AText.ofPlain(vessel.getAltitudeSpeed() + ""));
        AText size = AdventureMessageConfig.INFO_SIZE.parse(messages).withAllAs("%" + Message.VESSEL_SIZE.adapterText() + "%", AText.ofPlain(vessel.getStructure().getOriginalRelativePositions().size() + ""));

        viewer.sendMessage(infoName);
        viewer.sendMessage(maxSpeed);
        viewer.sendMessage(altitudeSpeed);
        viewer.sendMessage(size);

        if (vessel instanceof CrewStoredVessel) {
            CrewStoredVessel ship = (CrewStoredVessel) vessel;
            CrewPermission perm = ship.getDefaultPermission();
            AText permission = AdventureMessageConfig.INFO_DEFAULT_PERMISSION.parse(messages).withAllAs("%" + Message.CREW_ID.adapterText() + "%", AText.ofPlain(perm.getId()).withAllAs("%" + Message.CREW_NAME.adapterText() + "%", AText.ofPlain(perm.getName())));
            viewer.sendMessage(permission);
        }
        if (vessel instanceof ShipsVessel) {
            @NotNull Map<String, String> info = ((ShipsVessel) vessel).getExtraInformation();
            info.forEach((key, value) -> {
                AText built = AdventureMessageConfig.INFO_VESSEL_INFO.parse(messages).withAllAs(
                        "%" + Message.VESSEL_INFO_KEY.adapterText() + "%", AText.ofPlain(key)
                ).withAllAs(
                        "%" + Message.VESSEL_INFO_VALUE.adapterText() + "%", AText.ofPlain(value)
                );
                viewer.sendMessage(built);
            });
        }
        if (vessel instanceof ShipsVessel) {
            String flagIds = ArrayUtils.toString("\n - ", f -> {
                if (f instanceof VesselFlag.Serializable) {
                    return f.getId() + ": " + ((VesselFlag.Serializable<?>) f).serialize();
                }
                return f.getId();
            }, ((ShipsVessel) vessel).getFlags());
            String flagNames = ArrayUtils.toString("\n - ", f -> {
                if (f instanceof VesselFlag.Serializable) {
                    return f.getName() + ": " + ((VesselFlag.Serializable<?>) f).serialize();
                }
                return f.getName();
            }, ((ShipsVessel) vessel).getFlags());

            AText text = AdventureMessageConfig
                    .INFO_FLAG
                    .parse(messages)
                    .withAllAs(
                            "%" + Message.VESSEL_FLAG_ID.adapterText() + "%",
                            AText.ofPlain(flagIds))
                    .withAllAs(
                            "%" + Message.VESSEL_FLAG_NAME.adapterText() + "%",
                            AText.ofPlain(flagNames));
            viewer.sendMessage(text);
        }

        viewer.sendMessage(AdventureMessageConfig.INFO_ENTITIES_LINE.parse(messages));
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        vessel.getEntitiesOvertime(config.getEntityTrackingLimit(), e -> true, e -> {
            AText entitiesText = AdventureMessageConfig.INFO_ENTITIES_LIST.parse(messages);
            entitiesText = AdventureMessageConfig.INFO_ENTITIES_LIST.process(entitiesText, e);
            viewer.sendMessage(entitiesText);
        }, e -> {
        });
        return true;
    }
}
