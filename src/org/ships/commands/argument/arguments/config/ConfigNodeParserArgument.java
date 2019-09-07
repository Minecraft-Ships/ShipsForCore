package org.ships.commands.argument.arguments.config;

import org.core.command.argument.ArgumentContext;
import org.core.command.argument.CommandContext;
import org.core.configuration.parser.StringParser;
import org.ships.config.node.DedicatedNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ConfigNodeParserArgument implements ArgumentContext<String> {

    private String id;
    private String configId;
    private String nodeId;

    public ConfigNodeParserArgument(String id, String configId, String nodeId){
        this.id = id;
        this.configId = configId;
        this.nodeId = nodeId;
    }

    public String getConfigId(){
        return this.configId;
    }

    public String getNodeId(){
        return this.nodeId;
    }

    public Optional<StringParser<?>> getParser(CommandContext context){
        Optional<DedicatedNode<?>> opNode = context.getArgumentValue(this.nodeId);
        if(!opNode.isPresent()){
            return Optional.empty();
        }
        return Optional.of(opNode.get().getParser());
    }

    @Override
    public String parse(CommandContext context) throws IOException {
        return context.next();
    }

    @Override
    public List<String> getSuggestions(CommandContext context, String... args) {
        List<String> list = new ArrayList<>();
        context.getArgumentValue(this.nodeId).ifPresent(n -> {
            StringParser<?> parser = ((DedicatedNode<?>)n).getParser();
            if(parser instanceof StringParser.Suggestible){
                list.addAll(((StringParser.Suggestible<?>) parser).getStringSuggestions());
                list.sort(Comparator.naturalOrder());
            }
        });
        return list;
    }

    @Override
    public String getId() {
        return this.id;
    }
}
