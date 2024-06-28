package org.ships.commands.argument.ship.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.Messageable;
import org.core.source.command.CommandSource;
import org.core.utils.Else;
import org.core.utils.Identifiable;
import org.jetbrains.annotations.NotNull;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.config.messages.Message;
import org.ships.config.messages.Messages;
import org.ships.config.messages.adapter.MessageAdapters;
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
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShipsShipInfoArgumentCommand implements ArgumentCommand {

    private final String SHIP_ARGUMENT = "ship";
    private final String SHIP_ID_ARGUMENT = "ship_id";
    private final String SHIP_INFO_ARGUMENT = "info";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(this.SHIP_ARGUMENT), new ShipIdArgument<>(this.SHIP_ID_ARGUMENT),
                             new ExactArgument(this.SHIP_INFO_ARGUMENT));
    }

    @Override
    public String getDescription() {
        return "Information about the specified ship";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        Vessel vessel = commandContext.getArgument(this, this.SHIP_ID_ARGUMENT);
        displayInfo(commandContext.getSource(), vessel);
        return true;
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        return true;
    }

    private static <T> String flagToString(Function<? super VesselFlag<T>, String> to, VesselFlag<T> flag) {
        return to.apply(flag) + flag.getValue().map(v -> ": " + flag.getParser().unparse(v)).orElse("");
    }

    public static void displayInfo(Messageable viewer, Vessel vessel) {
        AdventureMessageConfig messages = ShipsPlugin.getPlugin().getAdventureMessageConfig();
        Component infoName = Messages.INFO_NAME
                .parseMessage(messages)
                .replaceText(TextReplacementConfig
                                     .builder()
                                     .match("%" + MessageAdapters.VESSEL_NAME.adapterText() + "%")
                                     .replacement(Else.throwOr(NoLicencePresent.class, vessel::getName, "Unknown"))
                                     .build());
        if (vessel instanceof IdentifiableShip) {
            IdentifiableShip ship = (IdentifiableShip)vessel;
            Component infoId = Messages.INFO_ID
                    .parseMessage(messages)
                    .replaceText(TextReplacementConfig
                                         .builder()
                                         .match("%" + MessageAdapters.VESSEL_ID.adapterText() + "%")
                                         .replacement(Else.throwOr(NoLicencePresent.class, ship::getId, "Unknown"))
                                         .build());
            viewer.sendMessage(infoId);
        }
        Component maxSpeed = Messages.INFO_MAX_SPEED
                .parseMessage(messages)
                .replaceText(TextReplacementConfig
                                     .builder()
                                     .match("%" + MessageAdapters.VESSEL_SPEED.adapterText() + "%")
                                     .replacement(vessel.getMaxSpeed() + "")
                                     .build());
        Component altitudeSpeed = Messages.INFO_ALTITUDE_SPEED
                .parseMessage(messages)
                .replaceText(TextReplacementConfig
                                     .builder()
                                     .match("%" + MessageAdapters.VESSEL_SPEED.adapterText() + "%")
                                     .replacement(vessel.getAltitudeSpeed() + "")
                                     .build());
        Component size = Messages.INFO_SIZE
                .parseMessage(messages)
                .replaceText(TextReplacementConfig
                                     .builder()
                                     .match("%" + MessageAdapters.STRUCTURE_SIZE.adapterText() + "%")
                                     .replacement(
                                             vessel.getStructure().size() + "")
                                     .build());

        viewer.sendMessage(infoName);
        viewer.sendMessage(maxSpeed);
        viewer.sendMessage(altitudeSpeed);
        viewer.sendMessage(size);

        if (vessel instanceof CrewStoredVessel) {
            CrewStoredVessel ship = (CrewStoredVessel)vessel;
            CrewPermission perm = ship.getDefaultPermission();

            Component replacementPermissionName = Component
                    .text(perm.getId())
                    .replaceText(TextReplacementConfig
                                         .builder()
                                         .match("%" + MessageAdapters.CREW_NAME.adapterText() + "%")
                                         .replacement(perm.getName())
                                         .build());

            Component permission = Messages.INFO_DEFAULT_PERMISSION
                    .parseMessage(messages)
                    .replaceText(TextReplacementConfig
                                         .builder()
                                         .match("%" + MessageAdapters.CREW_ID.adapterText() + "%")
                                         .replacement(replacementPermissionName)
                                         .build());
            viewer.sendMessage(permission);
        }
        if (vessel instanceof ShipsVessel) {
            @NotNull Map<String, String> info = ((ShipsVessel) vessel).getExtraInformation();
            info.forEach((key, value) -> {
                Component built = Messages.INFO_VESSEL_INFO
                        .parseMessage(messages)
                        .replaceText(TextReplacementConfig
                                             .builder()
                                             .match("%" + MessageAdapters.VESSEL_INFO_KEY.adapterText() + "%")
                                             .replacement(key)
                                             .build())
                        .replaceText(TextReplacementConfig
                                             .builder()
                                             .match("%" + MessageAdapters.VESSEL_INFO_VALUE.adapterText() + "%")
                                             .replacement(value)
                                             .build());
                viewer.sendMessage(built);
            });
        }
        if (vessel instanceof ShipsVessel) {
            String flagIds = ((ShipsVessel) vessel)
                    .getFlags()
                    .stream()
                    .map(vf -> flagToString(Identifiable::getId, vf))
                    .collect(Collectors.joining("\n - "));

            String flagNames = ((ShipsVessel) vessel)
                    .getFlags()
                    .stream()
                    .map(vf -> flagToString(Identifiable::getName, vf))
                    .collect(Collectors.joining("\n - "));

            Component text = Messages.INFO_FLAG
                    .parseMessage(messages)
                    .replaceText(TextReplacementConfig
                                         .builder()
                                         .match("%" + MessageAdapters.VESSEL_FLAG_ID.adapterText() + "%")
                                         .replacement(flagIds)
                                         .build())
                    .replaceText(TextReplacementConfig
                                         .builder()
                                         .match("%" + MessageAdapters.VESSEL_FLAG_NAME.adapterText() + "%")
                                         .replacement(flagNames)
                                         .build());
            viewer.sendMessage(text);
        }

        viewer.sendMessage(Messages.INFO_ENTITIES_LINE.parseMessage(messages));
        vessel.getEntitiesOvertime(e -> true).thenAccept(collection -> collection.forEach(entity -> {
            Component entitiesText = Messages.INFO_ENTITIES_LIST.parseMessage(messages);
            entitiesText = Messages.INFO_ENTITIES_LIST.processMessage(entitiesText, entity);
            viewer.sendMessage(entitiesText);
        }));
    }
}
