package org.ships.commands.argument.arguments.identifiable.crew;

import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.utils.lamda.tri.TriPredicate;
import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.permissions.vessel.CrewPermissions;

public class CrewPermissionArgument extends ShipIdentifiableArgument<CrewPermission> {
    public CrewPermissionArgument(String id) {
        super(id, () -> CrewPermissions.permissions().stream());
    }

    public CrewPermissionArgument(String id,
                                  TriPredicate<? super CommandContext, ? super CommandArgumentContext<CrewPermission>, ? super CrewPermission> predicate) {
        super(id, () -> CrewPermissions.permissions().stream(), predicate);
    }
}
