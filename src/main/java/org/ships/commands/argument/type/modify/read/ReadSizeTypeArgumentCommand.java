package org.ships.commands.argument.type.modify.read;

import org.core.adventureText.AText;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.assits.shiptype.SizedShipType;

import java.util.List;
import java.util.Optional;

public class ReadSizeTypeArgumentCommand implements ArgumentCommand {

    private final ExactArgument type = new ExactArgument("shiptype");
    private final ExactArgument modify = new ExactArgument("modify");
    private final ShipIdentifiableArgument<SizedShipType<?>> shipType = new ShipIdentifiableArgument<>("shiptype value",
                                                                                                       (Class<SizedShipType<?>>) (Object) SizedShipType.class,
                                                                                                       (c, a, t) -> true);
    private final ExactArgument get = new ExactArgument("get");
    private final ExactArgument size = new ExactArgument("size");


    @Override
    public List<CommandArgument<?>> getArguments() {
        return List.of(this.type, this.modify, this.shipType, this.get, this.size);
    }

    @Override
    public String getDescription() {
        return "Get the min and max size for a shiptype";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIPTYPE_MODIFY_READ);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        SizedShipType<?> shipType = commandContext.getArgument(this, this.shipType);
        int minSize = shipType.getMinSize();
        String maxSize = shipType.getMaxSize().stream().map(Object::toString).findAny().orElse("unspecified");
        CommandSource viewer = commandContext.getSource();
        viewer.sendMessage(AText.ofPlain("Minimum size: " + minSize));
        viewer.sendMessage(AText.ofPlain("Maximum size: " + maxSize));

        return true;
    }
}
