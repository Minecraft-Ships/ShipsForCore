package org.ships.commands.argument.arguments.config;

import org.core.command.argument.ArgumentContext;
import org.core.command.argument.CommandContext;
import org.ships.config.Config;
import org.ships.plugin.ShipsPlugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigArgument implements ArgumentContext<Config> {

    protected String id;

    public ConfigArgument(String id){
        this.id = id;
    }

    @Override
    public Config parse(CommandContext context) throws IOException {
        String config = context.next();
        if(config.equalsIgnoreCase("config")){
            return ShipsPlugin.getPlugin().getConfig();
        }
        throw new IOException("Unknown configuration");
    }

    @Override
    public List<String> getSuggestions(CommandContext context, String... args) {
        return Arrays.asList("config").stream().filter(c -> c.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return this.id;
    }
}
