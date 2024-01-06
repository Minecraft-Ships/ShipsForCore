package org.ships.commands.argument.info;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.platform.PlatformDetails;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;

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
        CommandSource source = commandContext.getSource();
        if (!(source instanceof CommandViewer viewer)) {
            return true;
        }
        Collection<ShipType<?>> shipTypes = ShipsPlugin.getPlugin().getAllShipTypes();
        viewer.sendMessage(AText.ofPlain("----[Ships]----").withColour(NamedTextColours.YELLOW));
        viewer.sendMessage(AText
                                   .ofPlain("Ships Version: ")
                                   .withColour(NamedTextColours.AQUA)
                                   .append(AText
                                                   .ofPlain(ShipsPlugin.getPlugin().getPluginVersion().asString())
                                                   .withColour(NamedTextColours.GOLD)));
        viewer.sendMessage(AText
                                   .ofPlain("Ships " + ShipsPlugin.PRERELEASE_TAG + " Version: ")
                                   .withColour(NamedTextColours.AQUA)
                                   .append(AText
                                                   .ofPlain(ShipsPlugin.PRERELEASE_VERSION + "")
                                                   .withColour(NamedTextColours.GOLD)));
        viewer.sendMessage(this.readVersion(TranslateCore.getPlatform().getDetails()));
        viewer.sendMessage(this.readVersion(TranslateCore.getPlatform().getTranslateCoreDetails()));
        viewer.sendMessage(this.readVersion(TranslateCore.getPlatform().getImplementationDetails()));
        viewer.sendMessage(AText
                                   .ofPlain("Vessel Types: ")
                                   .withColour(NamedTextColours.AQUA)
                                   .append(AText.ofPlain(shipTypes.size() + "").withColour(NamedTextColours.GOLD)));
        if (commandContext.getArgument(this, SHIP_TYPE_ARGUMENT) != null) {
            List<AText> typeText = shipTypes
                    .stream()
                    .map(s -> AText.ofPlain(s.getDisplayName()).withColour(NamedTextColours.GOLD))
                    .toList();
            AText text = null;
            for (ShipType<?> shipType : shipTypes) {
                AText displayName = AText.ofPlain(shipType.getDisplayName()).withColour(NamedTextColours.GOLD);
                if (text == null) {
                    text = displayName;
                    continue;
                }
                text = text.append(AText.ofPlain(" | ").withColour(NamedTextColours.GREEN)).append(displayName);
            }
            viewer.sendMessage(text);
        }
        return true;
    }

    private AText readVersion(PlatformDetails details) {
        return AText
                .ofPlain(details.getName() + ": ")
                .withColour(NamedTextColours.AQUA)
                .append(AText.ofPlain(details.getVersion().asString()).withColour(NamedTextColours.GOLD));
    }
}
