package org.ships.commands.argument.type;

import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.RemainingArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.SwitchableVessel;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ShipsDeleteShipTypeArgument implements ArgumentCommand {

    private static final String SHIP_TYPE = "shiptype";
    private static final String DELETE = "delete";
    private static final String CUSTOM_SHIP_TYPE = "custom_ship_type";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(SHIP_TYPE), new ExactArgument(DELETE), new RemainingArgument<>(CUSTOM_SHIP_TYPE, new ShipIdentifiableArgument<>(CUSTOM_SHIP_TYPE, CloneableShipType.class, t -> !t.getOriginType().equals(t))));
    }

    @Override
    public String getDescription() {
        return "Delete a shiptype";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIPTYPE_CREATE);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource source = commandContext.getSource();
        List<CloneableShipType> types = commandContext.getArgument(this, CUSTOM_SHIP_TYPE);
        types.forEach(type -> {
            Set<SwitchableVessel<?>> vessels = ShipsPlugin
                    .getPlugin()
                    .getVessels()
                    .stream()
                    .filter(v -> v.getType().equals(type))
                    .filter(v -> v instanceof SwitchableVessel)
                    .map(v -> (SwitchableVessel<?>) v)
                    .collect(Collectors.toSet());
            long count = vessels.stream().filter(v -> {
                try {
                    ((SwitchableVessel<CloneableShipType<?>>) v).setType((CloneableShipType<?>) type);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }).count();
            if (count != vessels.size()) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessagePlain("Could not delete. Could not convert all vessels into " + type.getOriginType().getId() + ". Did convert " + count);
                }
                return;
            }
            try {
                Files.delete(type.getFile().getFile().toPath());
            } catch (IOException e) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessagePlain("Could not delete. " + e.getMessage());
                }
                throw new IllegalStateException(e);
            }
            ShipsPlugin.getPlugin().unregister(type);
            if (source instanceof CommandViewer) {
                ((CommandViewer) source).sendMessagePlain("Deleted " + type.getDisplayName() + " deleted. All " + count + " ships are now " + type.getOriginType().getDisplayName());
            }
        });
        return true;
    }
}
