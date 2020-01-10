package org.ships.commands.argument.arguments.config;

import org.core.command.argument.ArgumentContext;
import org.core.command.argument.CommandContext;
import org.ships.config.Config;
import org.ships.config.node.DedicatedNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ConfigNodeArgument implements ArgumentContext<DedicatedNode<?>> {

    private String id;
    private String configId;

    public ConfigNodeArgument(String id, String configId){
        this.id = id;
        this.configId = configId;
    }

    public String getConfigId(){
        return this.configId;
    }

    public Optional<Config> getConfig(CommandContext commandContext){
        return commandContext.getArgumentValue(this.configId);
    }

    @Override
    public DedicatedNode<?> parse(CommandContext context) throws IOException {
        String id = context.next();
        Optional<Config> opConfig = getConfig(context);
        if (opConfig.isPresent()) {
            Config config = opConfig.get();
            if (config instanceof Config.CommandConfigurable) {
                Config.CommandConfigurable cconfig = (Config.CommandConfigurable) config;
                Optional<DedicatedNode<?>> node = cconfig.get(id);
                if(node.isPresent()){
                    return node.get();
                }
                throw new IOException("Unknown node");
            }
            throw new IOException("No nodes for config");
        }
        throw new IOException("Unknown config");
    }

    @Override
    public List<String> getSuggestions(CommandContext context, String... args) {
        Optional<Config> opConfig = getConfig(context);
        if (opConfig.isPresent()){
            Config config = opConfig.get();
            if(config instanceof Config.CommandConfigurable){
                Config.CommandConfigurable cconfig = (Config.CommandConfigurable) config;
                List<String> list = new ArrayList<>();
                cconfig.getNodes().stream().filter(c -> c.getSimpleName().toLowerCase().startsWith(args[0].toLowerCase())).forEach(c -> {
                   list.add(c.getSimpleName());
                });
                list.sort(Comparator.naturalOrder());
                return list;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public String getId() {
        return this.id;
    }
}
