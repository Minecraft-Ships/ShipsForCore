package org.ships.commands.argument.config;

import org.core.command.ChildArgumentCommandLauncher;
import org.core.command.argument.CommandContext;
import org.core.command.argument.arguments.child.ChildrenArgument;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.argument.arguments.config.ConfigArgument;
import org.ships.commands.argument.arguments.config.ConfigNodeArgument;
import org.ships.commands.argument.arguments.config.ConfigNodeParserArgument;
import org.ships.config.Config;
import org.ships.config.node.DedicatedNode;
import org.ships.permissions.Permissions;

import java.util.Optional;

public class ShipsConfigCommand extends ChildArgumentCommandLauncher.ChildOnly {

    public static final String CONFIG_ID = "config";
    public static final String NODE_ID = "node";
    public static final String VALUE_ID = "value";

    public static class SetArgument extends ChildArgumentCommandLauncher {

        public SetArgument(){
            super(new ConfigArgument(CONFIG_ID), new ConfigNodeArgument(NODE_ID, CONFIG_ID), new ConfigNodeParserArgument(VALUE_ID, CONFIG_ID, NODE_ID));
        }

        @Override
        protected boolean process(CommandContext context) {
            Optional<Config> opConfig = context.getArgumentValue(CONFIG_ID);
            if(!opConfig.isPresent()){
                return false;
            }
            Optional<DedicatedNode<?>> opNode = context.getArgumentValue(NODE_ID);
            if(!opNode.isPresent()){
                return false;
            }
            Optional<String> opValue = context.getArgumentValue(VALUE_ID);
            if(!opValue.isPresent()){
                return false;
            }
            if(!set(opConfig.get(), opNode.get(), opValue.get())){
                if (context.getSource() instanceof CommandViewer){
                    ((CommandViewer) context.getSource()).sendMessagePlain("Unknown value of " + opValue.get());
                }
                return false;
            }
            if (context.getSource() instanceof CommandViewer){
                ((CommandViewer) context.getSource()).sendMessagePlain("Value changed to " + opValue.get());
            }
            return true;
        }

        private <T extends Object> boolean set(Config config, DedicatedNode<T> node, String value){
            Optional<T> opParsed = node.getParser().parse(value);
            if(!opParsed.isPresent()){
                return false;
            }
            node.setValue(config.getFile(), opParsed.get());
            return true;
        }

        @Override
        public String getName() {
            return "set";
        }

        @Override
        public String getDescription() {
            return "Description";
        }

        @Override
        public boolean hasPermission(CommandSource source) {
            if(source instanceof LivePlayer){
                return ((LivePlayer) source).hasPermission(Permissions.CMD_CONFIG_SET);
            }
            return true;
        }
    }

    public ShipsConfigCommand(){
        super(new ChildrenArgument.Builder().register("set", new ShipsConfigCommand.SetArgument()).build());
    }

    @Override
    public String getName() {
        return "Config";
    }

    @Override
    public String getDescription() {
        return "config commands";
    }
}
