package org.ships.commands.argument.type.modify.read;

import net.kyori.adventure.text.Component;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.utils.Identifiable;
import org.core.world.position.block.BlockType;
import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.assits.shiptype.SpecialBlocksShipType;
import org.ships.vessel.common.requirement.SpecialBlocksRequirement;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReadSpecialBlocksTypeArgumentCommand implements ArgumentCommand {

    private final ExactArgument type = new ExactArgument("shiptype");
    private final ExactArgument modify = new ExactArgument("modify");
    private final ShipIdentifiableArgument<SpecialBlocksShipType<?>> shipType = new ShipIdentifiableArgument<>(
            "shiptype value", (Class<SpecialBlocksShipType<?>>) (Object) SpecialBlocksShipType.class,
            (c, a, t) -> true);
    private final ExactArgument get = new ExactArgument("get");
    private final ExactArgument specialBlocks = new ExactArgument("blocks");


    @Override
    public List<CommandArgument<?>> getArguments() {
        return List.of(this.type, this.modify, this.shipType, this.get, this.specialBlocks);
    }

    @Override
    public String getDescription() {
        return "Get the special block data for a shiptype";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIPTYPE_MODIFY_READ);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        SpecialBlocksShipType<?> shipType = commandContext.getArgument(this, this.shipType);
        SpecialBlocksRequirement specialBlocksRequirement = shipType.getSpecialBlocksRequirement();
        Collection<BlockType> specialBlocksTypes = specialBlocksRequirement.getBlocks();
        float percentage = specialBlocksRequirement.getPercentage();

        commandContext
                .getSource()
                .sendMessage(Component.text("Special Block Types: " + specialBlocksTypes
                        .stream()
                        .map(Identifiable::getName)
                        .collect(Collectors.joining(", "))));
        commandContext.getSource().sendMessage(Component.text("Percentage: " + percentage));

        return true;
    }
}
