package org.ships.commands.argument.arguments;

import org.core.command.argument.arguments.CommandArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.utils.Identifable;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ShipIdArgument<V extends Vessel> implements CommandArgument<V> {

    private final String id;
    private final Predicate<Vessel> predicate;
    private final Function<Vessel, String> failMessage;

    public ShipIdArgument(String id){
        this(id, v -> true, v -> "You Broke Logic...");
    }

    public ShipIdArgument(String id, Predicate<Vessel> predicate, Function<Vessel, String> failMessage) {
        this.id = id;
        this.predicate = predicate;
        this.failMessage = failMessage;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Map.Entry<V, Integer> parse(CommandContext context, CommandArgumentContext<V> argument) throws IOException {
        String id = context.getCommand()[argument.getFirstArgument()];
        Optional<Vessel> opVessel = ShipsPlugin.getPlugin().getVessels().stream().filter(v -> v instanceof Identifable).filter(v -> ((Identifable) v).getId().equalsIgnoreCase(id)).findAny();
        if (!(opVessel.isPresent())) {
            throw new IOException("No Vessel by that name");
        }
        if (!this.predicate.test(opVessel.get())) {
            throw new IOException(this.failMessage.apply(opVessel.get()));
        }
        return new AbstractMap.SimpleImmutableEntry<>((V)opVessel.get(), argument.getFirstArgument() + 1);
    }

    @Override
    public List<String> suggest(CommandContext commandContext, CommandArgumentContext<V> argument) {
        String peek = commandContext.getCommand()[argument.getFirstArgument()];
        return ShipsPlugin.getPlugin().getVessels().stream()
                .filter(v -> v instanceof Identifable)
                .filter(v -> {
                    if (((Identifable) v).getId().startsWith(peek.toLowerCase())) {
                        return true;
                    }
                    return v.getName().startsWith(peek.toLowerCase());
                }).filter(this.predicate).sorted((o1, o2) -> {
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
                }).map(v -> ((Identifable) v).getId()).collect(Collectors.toList());
    }
}
