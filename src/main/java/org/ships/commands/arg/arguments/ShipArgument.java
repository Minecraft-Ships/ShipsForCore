package org.ships.commands.arg.arguments;

import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentSnapshot;
import org.core.command.argument.CommandContext;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.command.CommandSource;
import org.core.utils.Identifable;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;

public class ShipArgument implements CommandArgument<Vessel> {

    public static final BiPredicate<CommandArgumentSnapshot, Vessel> NO_FILTER = ((s, v) -> true);
    public static final BiPredicate<CommandArgumentSnapshot, Vessel> CAPTAIN_OF = ((s, v) -> {
        if(!(s.getSource() instanceof LivePlayer)){
            return true;
        }
        if(!(v instanceof CrewStoredVessel)){
            return false;
        }
        return ((CrewStoredVessel) v).getUserCrew(CrewPermission.CAPTAIN).stream().anyMatch(u -> u.equals(s.getSource()));
    });
    public static final BiPredicate<CommandArgumentSnapshot, Vessel> MEMBER_OF = ((s, v) -> {
        if(!(s.getSource() instanceof LivePlayer)){
            return true;
        }
        if(!(v instanceof CrewStoredVessel)){
            return false;
        }
        return ((CrewStoredVessel) v).getUserCrew(CrewPermission.CREW_MEMBER).stream().anyMatch(u -> u.equals(s.getSource()));
    });

    private BiPredicate<CommandArgumentSnapshot, Vessel> filter;
    private String id;

    public ShipArgument(String id, BiPredicate<CommandArgumentSnapshot, Vessel> filter){
        this.id = id;
        this.filter = filter;
    }

    @Override
    public Set<String> getSuggestions(CommandSource source, int index, String... words) {
        CommandArgumentSnapshot snapshot = new CommandArgumentSnapshot(source, index, words);
        Set<String> set = new HashSet<>();
        ShipsPlugin.getPlugin().getVessels().stream().filter(v -> v instanceof Identifable).filter(v -> ((Identifable) v).getId().toLowerCase().startsWith(words[index])).filter(v -> this.filter.test(snapshot, v)).forEach(s -> set.add(((Identifable) s).getId()));
        return set;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public CommandContext.CommandArgumentEntry<Vessel> run(CommandContext context, CommandSource source, int index, String... words) throws IOException {
        CommandArgumentSnapshot snapshot = new CommandArgumentSnapshot(source, index, words);
        Optional<Vessel> opShip = ShipsPlugin.getPlugin().getVessels().stream().filter(v -> v instanceof Identifable).filter(v -> ((Identifable) v).getId().equalsIgnoreCase(words[index])).filter(v -> this.filter.test(snapshot, v)).findAny();
        if(opShip.isPresent()){
            return new CommandContext.CommandArgumentEntry<>(this, index, index + 1, opShip.get());
        }
        throw new IOException("Unknown vessel of " + words[index]);
    }
}
