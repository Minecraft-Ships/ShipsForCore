package org.ships.commands.argument.type.modify.read;

import net.kyori.adventure.text.Component;
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
import org.ships.vessel.common.types.ShipTypes;

import java.util.List;
import java.util.Optional;

public class ReadSizeTypeArgumentCommand implements ArgumentCommand {

    private final ExactArgument type = new ExactArgument("shiptype");
    private final ExactArgument modify = new ExactArgument("modify");
    private final ShipIdentifiableArgument<SizedShipType<?>> shipType = new ShipIdentifiableArgument<>("shiptype value",
                                                                                                       () -> ShipTypes
                                                                                                               .shipTypes()
                                                                                                               .stream()
                                                                                                               .filter(t -> t instanceof SizedShipType)
                                                                                                               .map(t -> (SizedShipType<?>) t),
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
        int minSize = shipType.getMinimumSize();
        String maxSize = shipType.getMaximumSize().stream().boxed().map(Object::toString).findAny().orElse("unspecified");
        CommandSource viewer = commandContext.getSource();
        viewer.sendMessage(Component.text("Minimum size: " + minSize));
        viewer.sendMessage(Component.text("Maximum size: " + maxSize));

        return true;
    }
}
