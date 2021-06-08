package org.ships.commands.argument.arguments;

import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentResult;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.command.CommandSource;
import org.core.utils.Else;
import org.ships.exceptions.NoLicencePresent;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.assits.IdentifiableShip;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShipIdArgument<V extends Vessel> implements CommandArgument<V> {

    private final String id;
    private final BiPredicate<CommandSource, Vessel> predicate;
    private final Function<Vessel, String> failMessage;

    public ShipIdArgument(String id) {
        this(id, (source, vessel) -> {
            if (source instanceof LivePlayer && vessel instanceof CrewStoredVessel) {
                CrewStoredVessel crewVessel = (CrewStoredVessel) vessel;
                LivePlayer player = (LivePlayer) source;
                return crewVessel.getPermission(player.getUniqueId()).canCommand();
            }
            return true;
        }, v -> "Your crew permission does not allow for commands");
    }

    public ShipIdArgument(String id, BiPredicate<CommandSource, Vessel> predicate, Function<Vessel, String> failMessage) {
        this.id = id;
        this.predicate = predicate;
        this.failMessage = failMessage;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public CommandArgumentResult<V> parse(CommandContext context, CommandArgumentContext<V> argument) throws IOException {
        String id = context.getCommand()[argument.getFirstArgument()];
        Optional<IdentifiableShip> opVessel = ShipsPlugin.getPlugin().getVessels().stream().filter(v -> v instanceof IdentifiableShip).map(v -> (IdentifiableShip) v).filter(v -> {
            try {
                return v.getId().equalsIgnoreCase(id);
            } catch (NoLicencePresent noLicencePresent) {
                return false;
            }
        }).findAny();
        if (!(opVessel.isPresent())) {
            throw new IOException("No Vessel by that name");
        }
        if (!this.predicate.test(context.getSource(), opVessel.get())) {
            throw new IOException(this.failMessage.apply(opVessel.get()));
        }
        return CommandArgumentResult.from(argument, (V) opVessel.get());
    }

    @Override
    public List<String> suggest(CommandContext commandContext, CommandArgumentContext<V> argument) {
        String peek = commandContext.getCommand()[argument.getFirstArgument()];
        return ShipsPlugin
                .getPlugin()
                .getVessels()
                .stream()
                .filter(v -> v instanceof IdentifiableShip)
                .map(v -> (IdentifiableShip) v)
                .filter(v -> {
                    try {
                        v.getId();
                        return true;
                    } catch (NoLicencePresent noLicencePresent) {
                        return false;
                    }
                })
                .filter(v -> {
                    try {
                        if (v.getId().startsWith(peek.toLowerCase())) {
                            return true;
                        }
                        return v.getName().startsWith(peek.toLowerCase());
                    } catch (NoLicencePresent e) {
                        return false;
                    }
                })
                .filter(v -> this.predicate.test(commandContext.getSource(), v))
                .sorted((o1, o2) -> {
                    if (!(commandContext.getSource() instanceof LivePlayer)) {
                        return 0;
                    }
                    LivePlayer player = (LivePlayer) commandContext.getSource();
                    if (o1 instanceof CrewStoredVessel) {
                        CrewPermission permission = ((CrewStoredVessel) o1).getCrew().get(player.getUniqueId());
                        if (permission != null && !permission.equals(CrewPermission.DEFAULT)) {
                            return 1;
                        }
                    }
                    if (o2 instanceof CrewStoredVessel) {
                        CrewPermission permission = ((CrewStoredVessel) o2).getCrew().get(player.getUniqueId());
                        if (permission != null && !permission.equals(CrewPermission.DEFAULT)) {
                            return -1;
                        }
                    }
                    return 0;
                })
                .filter(v -> Else.throwOr(NoLicencePresent.class, () -> {
                    v.getId();
                    return true;
                }, false)).map(v -> {
                    try {
                        return v.getId();
                    } catch (NoLicencePresent noLicencePresent) {
                        noLicencePresent.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }
}
