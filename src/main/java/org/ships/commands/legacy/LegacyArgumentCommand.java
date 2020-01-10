package org.ships.commands.legacy;

import org.core.source.command.CommandSource;

import java.util.List;
import java.util.Optional;

public interface LegacyArgumentCommand {

    String getName();
    Optional<String> getPermission();
    boolean run(CommandSource source, String... args);
    List<String> tab(CommandSource source, String... args);
}
