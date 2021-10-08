package org.ships.commands.argument.type;

import org.core.TranslateCore;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.RemainingArgument;
import org.core.command.argument.arguments.simple.StringArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShipsCreateShipTypeArgument implements ArgumentCommand {

    private static final String SHIP_TYPE = "shiptype";
    private static final String CREATE = "create";
    private static final String CLONEABLE_SHIP_TYPE = "cloneable_ship_type";
    private static final String NEW_SHIP_TYPE_NAME = "new_ship_type_name";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(
                new ExactArgument(SHIP_TYPE),
                new ExactArgument(CREATE),
                new ShipIdentifiableArgument<>(CLONEABLE_SHIP_TYPE, CloneableShipType.class),
                new RemainingArgument<>(NEW_SHIP_TYPE_NAME, new StringArgument(NEW_SHIP_TYPE_NAME)));
    }

    @Override
    public String getDescription() {
        return "Creates custom Ship Type";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIPTYPE_CREATE);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource source = commandContext.getSource();
        CloneableShipType<?> type = ((CloneableShipType<?>) commandContext.getArgument(this, CLONEABLE_SHIP_TYPE)).getOriginType();
        List<String> names = commandContext.getArgument(this, NEW_SHIP_TYPE_NAME);
        names.forEach(name -> {
            File file = new File(ShipsPlugin
                    .getPlugin()
                    .getShipsConigFolder(),
                    "Configuration/ShipType/Custom/"
                            + type
                            .getId()
                            .replace(":", ".")
                            + "/"
                            + name
                            + "."
                            + TranslateCore
                            .getPlatform()
                            .getConfigFormat()
                            .getFileType()[0]);
            file = TranslateCore.createConfigurationFile(file, TranslateCore.getPlatform().getConfigFormat()).getFile();
            if (file.exists()) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessagePlain("Custom ShipType " + name + " has already been created");
                }
                return;
            }
            try {
                file.getParentFile().mkdirs();
                Files.copy(type.getFile().getFile().toPath(), file.toPath());
            } catch (IOException e) {
                if (source instanceof CommandViewer) {
                    ((CommandViewer) source).sendMessagePlain(name + " failed to created file. " + e.getMessage());
                }
                e.printStackTrace();
            }
            CloneableShipType<?> newType = type.cloneWithName(file, name);
            ShipsPlugin.getPlugin().register(newType);
            if (source instanceof CommandViewer) {
                ((CommandViewer) source).sendMessagePlain(name + " created. ");
            }
        });

        return true;
    }
}
