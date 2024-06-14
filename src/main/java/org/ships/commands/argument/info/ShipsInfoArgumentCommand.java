package org.ships.commands.argument.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.TranslateCore;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.platform.PlatformDetails;
import org.core.source.command.CommandSource;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipTypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ShipsInfoArgumentCommand implements ArgumentCommand {

    private static final String INFO_ARGUMENT = "info";
    private static final String SHIP_TYPE_ARGUMENT = "ship_type_flag";

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument(INFO_ARGUMENT),
                             new OptionalArgument<>(new ExactArgument(SHIP_TYPE_ARGUMENT, false, "shipstype", "stype"),
                                                    (String) null));
    }

    @Override
    public String getDescription() {
        return "Basic information about the Ships plugin";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_INFO);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        CommandSource viewer = commandContext.getSource();
        Collection<ShipType<?>> shipTypes = ShipTypes.shipTypes();
        viewer.sendMessage(Component.text("----[Ships]----").color(NamedTextColor.YELLOW));
        viewer.sendMessage(Component
                                   .text("Ships Version: ")
                                   .color(NamedTextColor.AQUA)
                                   .append(Component
                                                   .text(ShipsPlugin.getPlugin().getPluginVersion().asString())
                                                   .color(NamedTextColor.GOLD)));
        viewer.sendMessage(Component
                                   .text("Ships " + ShipsPlugin.PRERELEASE_TAG + " Version: ")
                                   .color(NamedTextColor.AQUA)
                                   .append(Component
                                                   .text(ShipsPlugin.PRERELEASE_VERSION + "")
                                                   .color(NamedTextColor.GOLD)));
        viewer.sendMessage(this.readVersion(TranslateCore.getPlatform().getDetails()));
        viewer.sendMessage(this.readVersion(TranslateCore.getPlatform().getTranslateCoreDetails()));
        viewer.sendMessage(this.readVersion(TranslateCore.getPlatform().getImplementationDetails()));
        viewer.sendMessage(Component
                                   .text("Vessel Types: ")
                                   .color(NamedTextColor.AQUA)
                                   .append(Component.text(shipTypes.size() + "").color(NamedTextColor.GOLD)));
        if (commandContext.getArgument(this, SHIP_TYPE_ARGUMENT) != null) {
            Component text = null;
            for (ShipType<?> shipType : shipTypes) {
                Component displayName = Component.text(shipType.getDisplayName()).color(NamedTextColor.GOLD);
                if (text == null) {
                    text = displayName;
                    continue;
                }
                text = text.append(Component.text(" | ").color(NamedTextColor.GREEN)).append(displayName);
            }
            if (text != null) {
                viewer.sendMessage(text);
            }
        }
        return true;
    }

    private Component readVersion(PlatformDetails details) {
        return Component
                .text(details.getName() + ": ")
                .color(NamedTextColor.AQUA)
                .append(Component.text(details.getVersion().asString()).color(NamedTextColor.GOLD));
    }
}
