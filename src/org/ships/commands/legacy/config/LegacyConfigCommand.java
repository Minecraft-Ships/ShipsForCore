package org.ships.commands.legacy.config;

import org.core.configuration.parser.StringParser;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.config.Config;
import org.ships.config.node.DedicatedNode;
import org.ships.plugin.ShipsPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LegacyConfigCommand implements LegacyArgumentCommand {
    @Override
    public String getName() {
        return "config";
    }

    @Override
    public boolean run(CommandSource source, String... args) {
        if(args.length >= 4){
            String tag = args[1];
            if(!(tag.equalsIgnoreCase("set") || tag.equalsIgnoreCase("view"))){
                if(source instanceof CommandViewer){
                    ((CommandViewer) source).sendMessagePlain("Must be either set or view");
                }
                return false;
            }
            String configName = args[2];
            Config.CommandConfigurable config;
            switch(configName.toLowerCase()){
                case "config": config = ShipsPlugin.getPlugin().getConfig(); break;
                default:
                    if(source instanceof CommandViewer){
                        ((CommandViewer) source).sendMessagePlain("Unknown config");
                    }
                    return false;
            }
            if(tag.equalsIgnoreCase("set") && args.length >= 5) {
                if (setNode(config, args[3], args[4])){
                    return true;
                }
                if(source instanceof CommandViewer){
                    ((CommandViewer) source).sendMessagePlain("Unknown key or value");
                }
            }else if(tag.equalsIgnoreCase("view")){
                Optional<DedicatedNode<?>> opNode = config.get(args[3]);
                if(!opNode.isPresent()){
                    if(source instanceof CommandViewer){
                        ((CommandViewer) source).sendMessagePlain("Unknown key");
                    }
                    return false;
                }
                String text = readNode(config, opNode.get());
                if(source instanceof CommandViewer){
                    ((CommandViewer) source).sendMessagePlain(args[3] + ": " + text);
                    return true;
                }
                return false;
            }
            return false;
        }
        if(source instanceof CommandViewer){
            ((CommandViewer) source).sendMessagePlain("/ships config <set/view> <config> <key> <value>");
        }
        return false;
    }

    @Override
    public List<String> tab(CommandSource source, String... args) {
        if(args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase(""))){
            return Arrays.asList("set", "view");
        }
        if(args.length == 2){
            List<String> ret = new ArrayList<>();
            if("set".startsWith(args[1].toLowerCase())){
                ret.add("set");
            }
            if("view".startsWith(args[1].toLowerCase())){
                ret.add("view");
            }
            return ret;
        }
        if(args.length == 2 || args.length == 3 && args[2].equalsIgnoreCase("")){
            return Arrays.asList("config");
        }
        if(args.length == 3){
            return Arrays.asList("config");
        }
        if(args.length == 3 || args.length == 4 && args[3].equalsIgnoreCase("")){
            Config.CommandConfigurable config;
            switch(args[2].toLowerCase()){
                case "config": config = ShipsPlugin.getPlugin().getConfig(); break;
                default: return new ArrayList<>();
            }
            List<String> list = new ArrayList<>();
            config.getNodes().stream().forEach(dn -> list.add(dn.getSimpleName()));
            return list;
        }
        if(args.length == 4){
            Config.CommandConfigurable config;
            switch(args[2].toLowerCase()){
                case "config": config = ShipsPlugin.getPlugin().getConfig(); break;
                default: return new ArrayList<>();
            }
            List<String> list = new ArrayList<>();
            config.getNodes().stream().filter(dn -> dn.getSimpleName().toLowerCase().startsWith(args[3].toLowerCase())).forEach(dn -> list.add(dn.getSimpleName()));
            return list;
        }
        if((args.length == 4 || (args.length == 5 && args[4].equalsIgnoreCase(""))) && args[1].toLowerCase().equals("set")){
            Config.CommandConfigurable config;
            switch(args[2].toLowerCase()){
                case "config": config = ShipsPlugin.getPlugin().getConfig(); break;
                default:
                    return new ArrayList<>();
            }
            Optional<DedicatedNode<?>> opNode = config.get(args[3]);
            if(!opNode.isPresent()){
                return new ArrayList<>();
            }
            StringParser parser = opNode.get().getParser();
            if(parser instanceof StringParser.Suggestible){
                return ((StringParser.Suggestible) parser).getStringSuggestions();
            }
        }
        if(args.length == 5 && args[1].toLowerCase().equals("set")){
            Config.CommandConfigurable config;
            switch(args[2].toLowerCase()){
                case "config": config = ShipsPlugin.getPlugin().getConfig(); break;
                default:
                    return new ArrayList<>();
            }
            Optional<DedicatedNode<?>> opNode = config.get(args[3]);
            if(!opNode.isPresent()){
                return new ArrayList<>();
            }
            StringParser parser = opNode.get().getParser();
            if(parser instanceof StringParser.Suggestible){
                return ((StringParser.Suggestible) parser).getStringSuggestions(args[4]);
            }
        }
        return new ArrayList<>();
    }

    private <T> String readNode(Config.CommandConfigurable config, DedicatedNode<T> node){
        Optional<T> opValue = node.getValue(config.getFile());
        if(!opValue.isPresent()){
            return "<no value>";
        }
        return node.getParser().unparse(opValue.get());
    }

    private <T> boolean setNode(Config.CommandConfigurable config, String arg, String value){
        Optional<DedicatedNode<?>> opDedicated = config.get(arg);
        if(!opDedicated.isPresent()){
            return false;
        }
        DedicatedNode<T> dedicated = (DedicatedNode<T>) opDedicated.get();
        Optional<T> opResult = dedicated.getParser().parse(value);
        if(opResult.isPresent()){
            dedicated.setValue(config.getFile(), opResult.get());
            config.getFile().save();
            return true;
        }
        return false;
    }
}
