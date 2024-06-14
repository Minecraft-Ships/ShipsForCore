package org.ships.commands.argument.type.flag;

import net.kyori.adventure.text.Component;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.Messageable;
import org.core.source.command.CommandSource;
import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipTypes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ViewShipTypeFlagArgument implements ArgumentCommand {

    private final ExactArgument SHIP_TYPE_KEY_ARGUMENT = new ExactArgument("ship type key", false, "shiptype");
    private final ExactArgument FLAG_KEY_ARGUMENT = new ExactArgument("flag key", false, "flag");
    private final ExactArgument VIEW_KEY = new ExactArgument("view");
    private final ShipIdentifiableArgument<ShipType<?>> SHIP_TYPE = new ShipIdentifiableArgument<>("shiptype",
                                                                                                   () -> ShipTypes
                                                                                                           .shipTypes()
                                                                                                           .stream(),
                                                                                                   (c, a, v) -> !v
                                                                                                           .getFlags()
                                                                                                           .isEmpty());

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(this.SHIP_TYPE_KEY_ARGUMENT, this.FLAG_KEY_ARGUMENT, this.VIEW_KEY, this.SHIP_TYPE);
    }

    @Override
    public String getDescription() {
        return "View all flags on a vessel type";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIPTYPE_VIEW_FLAG);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        ShipType<?> type = commandContext.getArgument(this, this.SHIP_TYPE);
        CommandSource source = commandContext.getSource();
        type.getFlags().forEach(vf -> this.sendMessage(source, vf));
        return true;
    }

    private <F> void sendMessage(Messageable source, VesselFlag<F> flag) {
        source.sendMessage(
                Component.text(flag.getId() + ": " + flag.getValue().map(f -> flag.getParser().unparse(f)).orElse("")));

    }
}
