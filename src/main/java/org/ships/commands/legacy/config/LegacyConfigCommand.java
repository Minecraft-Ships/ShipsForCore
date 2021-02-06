package org.ships.commands.legacy.config;

import org.array.utils.ArrayUtils;
import org.core.CorePlugin;
import org.core.config.ConfigurationNode;
import org.core.config.parser.StringParser;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.core.text.TextColours;
import org.ships.commands.legacy.LegacyArgumentCommand;
import org.ships.config.Config;
import org.ships.config.messages.MessageConfig;
import org.ships.config.node.DedicatedNode;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;

import java.io.IOException;
import java.util.*;

@Deprecated
public class LegacyConfigCommand implements LegacyArgumentCommand {
    @Override
    public String getName() {
        return "config";
    }

    @Override
    public Optional<String> getPermission() {
        return Optional.empty();
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
            Config.KnownNodes config;
            switch(configName.toLowerCase()){
                case "config": config = ShipsPlugin.getPlugin().getConfig(); break;
                case "messages": config = ShipsPlugin.getPlugin().getMessageConfig(); break;
                default:
                    if(source instanceof CommandViewer){
                        ((CommandViewer) source).sendMessagePlain("Unknown config");
                    }
                    return false;
            }
            if(tag.equalsIgnoreCase("set") && args.length >= 5) {
                if (source instanceof LivePlayer && !((LivePlayer)source).hasPermission(Permissions.CMD_CONFIG_SET)){
                    ((LivePlayer) source).sendMessage(CorePlugin.buildText(TextColours.RED + "You do not have permission for that command"));
                    return false;
                }
                String remaining = ArrayUtils.toString(" ", t -> t, ArrayUtils.filter(4, args.length, args));
                try {
                    setNode(config, args[3], remaining);
                    return true;
                } catch (IOException e) {
                    if(source instanceof CommandViewer){
                        ((CommandViewer) source).sendMessagePlain(e.getMessage());
                    }
                }
            }else if(tag.equalsIgnoreCase("view")){
                if (source instanceof LivePlayer && !((LivePlayer)source).hasPermission(Permissions.CMD_CONFIG_VIEW)){
                    ((LivePlayer) source).sendMessage(CorePlugin.buildText(TextColours.RED + "You do not have permission for that command"));
                    return false;
                }
                Optional<DedicatedNode<Object, Object, ConfigurationNode.KnownParser<String, Object>>> opNode = config.getNode(args[3]);
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
        if(args.length == 3 && args[2].equalsIgnoreCase("")){
            return Arrays.asList("config", "messages");
        }
        if(args.length == 3){
            return Arrays.asList("config", "messages");
        }
        if(args.length == 4 && args[3].equalsIgnoreCase("")){
            Config.KnownNodes config;
            switch(args[2].toLowerCase()){
                case "config": config = ShipsPlugin.getPlugin().getConfig(); break;
                case "messages": config = ShipsPlugin.getPlugin().getMessageConfig(); break;
                default: return new ArrayList<>();
            }
            List<String> list = new ArrayList<>();
            config.getNodes().forEach(dn -> list.add(dn.getKeyName()));
            return list;
        }
        if(args.length == 4){
            Config.KnownNodes config;
            switch(args[2].toLowerCase()){
                case "config": config = ShipsPlugin.getPlugin().getConfig(); break;
                case "messages": config = ShipsPlugin.getPlugin().getMessageConfig(); break;
                default: return new ArrayList<>();
            }
            List<String> list = new ArrayList<>();
            config.getNodes().stream().filter(dn -> dn.getKeyName().toLowerCase().startsWith(args[3].toLowerCase())).forEach(dn -> list.add(dn.getKeyName()));
            return list;
        }
        if(((args.length == 5 && args[4].equalsIgnoreCase(""))) && args[1].toLowerCase().equals("set")){
            Config.KnownNodes config;
            switch(args[2].toLowerCase()){
                case "config": config = ShipsPlugin.getPlugin().getConfig(); break;
                case "messages": config = ShipsPlugin.getPlugin().getMessageConfig(); break;
                default:
                    return new ArrayList<>();
            }
            Optional<DedicatedNode<Object, Object, ConfigurationNode.KnownParser<String, Object>>> opNode = config.getNode(args[3]);
            if(!opNode.isPresent()){
                return new ArrayList<>();
            }
            if(config instanceof MessageConfig){
                Set<String> set = ((MessageConfig)config).getSuggestions(new ConfigurationNode(opNode.get().getNode().getPath()));
                List<String> list = new ArrayList<>(set);
                list.sort(Comparator.naturalOrder());
                return list;
            }
            StringParser<?> parser = (StringParser<?>) opNode.get().getNode().getParser();
            if(parser instanceof StringParser.Suggestible){
                return ((StringParser.Suggestible<?>) parser).getStringSuggestions();
            }
        }
        if(args.length >= 5 && args[1].toLowerCase().equals("set") && args[2].toLowerCase().equals("messages")){
            MessageConfig config = ShipsPlugin.getPlugin().getMessageConfig();
            Optional<DedicatedNode<Object, Object, ConfigurationNode.KnownParser<String, Object>>> opNode = config.getNode(args[3]);
            if(!opNode.isPresent()){
                return new ArrayList<>();
            }
            Set<String> set = config.getSuggestions(new ConfigurationNode(opNode.get().getNode().getPath()));
            List<String> list = new ArrayList<>(set);
            list.sort(Comparator.naturalOrder());
            return list;
        }
        if(args.length == 5 && args[1].toLowerCase().equals("set")){
            Config.KnownNodes config;
            switch(args[2].toLowerCase()){
                case "config": config = ShipsPlugin.getPlugin().getConfig(); break;
                case "messages": config = ShipsPlugin.getPlugin().getMessageConfig(); break;
                default:
                    return new ArrayList<>();
            }
            Optional<DedicatedNode<Object, Object, ConfigurationNode.KnownParser<String, Object>>> opNode = config.getNode(args[3]);
            if(!opNode.isPresent()){
                return new ArrayList<>();
            }
            StringParser<?> parser = (StringParser<?>) opNode.get().getNode().getParser();
            if(config instanceof MessageConfig){
                Set<String> set = ((MessageConfig)config).getSuggestions(new ConfigurationNode(opNode.get().getNode().getPath()));
                List<String> list = new ArrayList<>(set);
                list.sort(Comparator.naturalOrder());
                return list;
            }
            if(parser instanceof StringParser.Suggestible){
                return ((StringParser.Suggestible<?>) parser).getStringSuggestions(args[4]);
            }
        }
        return new ArrayList<>();
    }

    private <T> String readNode(Config.KnownNodes config, DedicatedNode<T, T, ConfigurationNode.KnownParser<String, T>> node){
        Optional<T> opValue = config.getFile().parse(node.getNode());
        if(!opValue.isPresent()){
            return "<no value>";
        }
        return node.getNode().getParser().unparse(opValue.get());
    }

    private <T> void setNode(Config.KnownNodes config, String arg, String value) throws IOException {
        Optional<DedicatedNode<Object, Object, ConfigurationNode.KnownParser<String, Object>>> opDedicated = config.getNode(arg);
        if(!opDedicated.isPresent()){
            throw new IOException("Unknown Key");
        }
        DedicatedNode<T, T, ConfigurationNode.KnownParser<String, T>> dedicated = (DedicatedNode<T, T, ConfigurationNode.KnownParser<String, T>>)(Object) opDedicated.get();
        Optional<T> opResult = dedicated.getNode().getParser().parse(value);
        if(opResult.isPresent()){
            dedicated.apply(config.getFile(), opResult.get());
            config.getFile().save();
            return;
        }
        throw new IOException("Unknown Value");
    }
}
