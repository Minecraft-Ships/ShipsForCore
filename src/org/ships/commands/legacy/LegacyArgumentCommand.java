package org.ships.commands.legacy;

import org.core.source.command.CommandSource;

import java.util.List;

public interface LegacyArgumentCommand {

    String getName();
    boolean run(CommandSource source, String... args);
    List<String> tab(CommandSource source, String... args);
}
