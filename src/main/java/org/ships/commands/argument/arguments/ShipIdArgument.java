package org.ships.commands.argument.arguments;

import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentResult;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.User;
import org.core.source.command.CommandSource;
import org.core.utils.Else;
import org.ships.exceptions.NoLicencePresent;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.assits.IdentifiableShip;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShipIdArgument<V extends Vessel> implements CommandArgument<V> {

    private final String id;
    private final BiPredicate<? super CommandSource, ? super Vessel> predicate;
    private final Function<? super Vessel, String> failMessage;

    public ShipIdArgument(String id) {
        this(id, (source, vessel) -> {
            if (source instanceof LivePlayer && vessel instanceof CrewStoredVessel crewVessel) {
                User player = (User) source;
                return crewVessel.getPermission(player.getUniqueId()).canCommand();
            }
            return true;
        }, v -> "Your crew permission does not allow for commands");
    }

    public ShipIdArgument(String id,
                          BiPredicate<? super CommandSource, ? super Vessel> predicate,
                          Function<? super Vessel, String> failMessage) {
        this.id = id;
        this.predicate = predicate;
        this.failMessage = failMessage;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public CommandArgumentResult<V> parse(CommandContext context, CommandArgumentContext<V> argument)
            throws IOException {
        String id = context.getCommand()[argument.getFirstArgument()];
        Optional<IdentifiableShip> opVessel = ShipsPlugin
                .getPlugin()
                .getVessels()
                .stream()
                .filter(v -> v instanceof IdentifiableShip)
                .map(v -> (IdentifiableShip) v)
                .filter(v -> {
                    try {
                        return v.getId().equalsIgnoreCase(id);
                    } catch (NoLicencePresent noLicencePresent) {
                        return false;
                    }
                })
                .findAny();
        if (opVessel.isEmpty()) {
            throw new IOException("No Vessel by that name");
        }
        if (!this.predicate.test(context.getSource(), opVessel.get())) {
            throw new IOException(this.failMessage.apply(opVessel.get()));
        }
        return CommandArgumentResult.from(argument, (V) opVessel.get());
    }

    @Override
    public Set<String> suggest(CommandContext commandContext, CommandArgumentContext<V> argument) {
        return ShipsPlugin
                .getPlugin()
                .getVessels()
                .stream()
                .filter(v -> v instanceof IdentifiableShip)
                .map(v -> Else.throwOr(NoLicencePresent.class, () -> {
                    IdentifiableShip ship = (IdentifiableShip) v;
                    return new AbstractMap.SimpleImmutableEntry<>(ship.getId(), ship);
                }, null))
                .filter(Objects::nonNull)
                .filter(v -> this.predicate.test(commandContext.getSource(), v.getValue()))
                .map(AbstractMap.SimpleImmutableEntry::getKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
