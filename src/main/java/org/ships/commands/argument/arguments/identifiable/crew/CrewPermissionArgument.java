package org.ships.commands.argument.arguments.identifiable.crew;

import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.utils.lamda.tri.TriPredicate;
import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.permissions.vessel.CrewPermission;

public class CrewPermissionArgument extends ShipIdentifiableArgument<CrewPermission> {
    public CrewPermissionArgument(String id, Class<CrewPermission> type) {
        super(id, type);
    }

    public CrewPermissionArgument(String id,
            Class<CrewPermission> type,
            TriPredicate<? super CommandContext, ?
                    super CommandArgumentContext<CrewPermission>, ? super CrewPermission> predicate) {
        super(id, type, predicate);
    }
}
