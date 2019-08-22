package org.ships.commands.argument.shiptype;

import org.core.CorePlugin;
import org.core.command.ChildArgumentCommandLauncher;
import org.core.command.argument.CommandContext;
import org.core.command.argument.arguments.child.ChildrenArgument;
import org.core.command.argument.arguments.generic.StringArgument;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.argument.arguments.ship.type.HostCloneableShipTypeArgument;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class ShipsShipTypeCommand extends ChildArgumentCommandLauncher.ChildOnly {

    private static final String HOST_SHIP_TYPE = "HostShipType";
    private static final String NEW_SHIP_TYPE = "NewShipType";

    public static class Create extends ChildArgumentCommandLauncher {

        public Create(){
            super(new HostCloneableShipTypeArgument(HOST_SHIP_TYPE), new StringArgument(NEW_SHIP_TYPE));
        }

        @Override
        protected boolean process(CommandContext context) {
            CommandSource source = context.getSource();
            Optional<CloneableShipType> opOriginalShipType = context.getArgumentValue(HOST_SHIP_TYPE);
            Optional<String> opNewName = context.getArgumentValue(NEW_SHIP_TYPE);
            if(!opNewName.isPresent()){
                if(source instanceof CommandViewer){
                    ((CommandViewer) source).sendMessagePlain("A name for the new ship type must be provided");
                }
                return false;
            }
            if(!opOriginalShipType.isPresent()){
                if(source instanceof CommandViewer){
                    ((CommandViewer) source).sendMessagePlain("A host ship type must be provided");
                }
                return false;
            }
            CloneableShipType type = opOriginalShipType.get();
            String name = opNewName.get();
            if(name.replaceAll(" ", "").length() == 0){
                if(source instanceof CommandViewer){
                    ((CommandViewer) source).sendMessagePlain("A name for the new ship type must be provided");
                }
                return false;
            }
            File file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "Configuration/ShipType/Custom/" + type.getOriginType().getId().replace(":", ".") + "/" + name + ".temp");
            file = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.DEFAULT).getFile();
            if(file.exists()){
                if(source instanceof CommandViewer){
                    ((CommandViewer) source).sendMessagePlain("Custom ShipType " + name + " has already been created");
                }
                return false;
            }
            try {
                file.getParentFile().mkdirs();
                Files.copy(type.getOriginType().getFile().getFile().toPath(), file.toPath());
            } catch (IOException e) {
                if(source instanceof CommandViewer){
                    ((CommandViewer) source).sendMessagePlain(name + " failed to created file. " + e.getMessage());
                }
                e.printStackTrace();
            }
            CloneableShipType newType = type.getOriginType().cloneWithName(file, name);
            ShipsPlugin.getPlugin().register(newType);
            if(source instanceof CommandViewer){
                ((CommandViewer) source).sendMessagePlain(name + " created. ");
            }
            return true;
        }

        @Override
        public String getName() {
            return "create";
        }

        @Override
        public String getDescription() {
            return "Create cloned ship types";
        }

        @Override
        public Optional<String> getPermission() {
            return Optional.of(Permissions.CMD_SHIPTYPE_CREATE);
        }
    }

    public ShipsShipTypeCommand(){
        super(new ChildrenArgument.Builder().register("create", new ShipsShipTypeCommand.Create()).build());
    }

    @Override
    public String getName() {
        return "shiptype";
    }

    @Override
    public String getDescription() {
        return "All commands relating to Ship types";
    }

    @Override
    public Optional<String> getPermission() {
        return Optional.empty();
    }
}
